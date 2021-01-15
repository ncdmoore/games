package engima.waratsea.model.squadron;

import engima.waratsea.model.aircraft.Attack;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.aircraft.Performance;
import engima.waratsea.model.aircraft.data.AttackData;
import lombok.Getter;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * The configuration of the squadron. These are mutually exclusive configurations.
 *
 * The priority of the configuration controls the desirability of the configuration.
 * The lower priority numbers are more desirable.
 *
 * For example, if both the NONE and the LEAN_ENGINE configurations can be applied to a squadron,
 * then the NONE configuration is applied as it has the higher priority.
 */
public enum SquadronConfig implements Comparator<SquadronConfig> {
    // No special configuration.
    NONE("Basic", 1) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            return attack;
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance();
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            return performance.getFerryDistance();
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            return performance.getRadius();
        }
    },

    // The squadron is equipped with drop tanks - this extends the squadron's range. Exclusive to fighters.
    DROP_TANKS("Drop Tanks", 2) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            return attack;
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance();
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            return (int) Math.ceil(performance.getFerryDistance() * DROP_TANK_FACTOR);
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            return (int) Math.ceil(performance.getRadius() * DROP_TANK_FACTOR);
        }
    },

    // The squadron is both equipped with extra internal fuel and is running very lean to extend its range. Any payload is reduced.
    LEAN_ENGINE("Long Range", 3) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            return attack.getReducedRoundUp(LEAN_ENGINE_FACTOR);
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance() * LEAN_ENGINE_FACTOR;
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            return performance.getFerryDistance() * LEAN_ENGINE_FACTOR;
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            return performance.getRadius() * LEAN_ENGINE_FACTOR;
        }
    },

    // The squadron has a reduced payload to allow for extra fuel.
    REDUCED_PAYLOAD("Reduced Payload", 4) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            return (type == AttackType.AIR) ? attack : attack.getReducedRoundDown(REDUCED_FACTOR);
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance();
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            int modifier = getReducedPayloadModifier(land, naval);
            return performance.getFerryDistance() + (modifier * 2);
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            int modifier = getReducedPayloadModifier(land, naval);
            return performance.getRadius() + modifier;
        }
    },

    // The squadron is equipped for a search. No ordinance and extra fuel.
    SEARCH("Search", 5) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            return (type == AttackType.AIR) ? attack : attack.getReducedRoundDown(SEARCH_FACTOR);
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance();
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            int modifier = getSearchModifier(land, naval);
            return performance.getFerryDistance() + (modifier * 2);
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            int modifier = getSearchModifier(land, naval);
            return performance.getRadius() + modifier;
        }
    },

    // The squadron is stripped of ordinance and is loaded with extra fuel. This extends the squadron's range. Exclusive to fighters.
    STRIPPED_DOWN("Stripped Down", 6) {

        public Attack getAttack(final AttackType type, final Attack attack) {
            AttackData data = new AttackData();
            if (type == AttackType.AIR) {             // a zero attack modifier.
                data.setFull(attack.getFull());
                data.setHalf(attack.getHalf());
                data.setSixth(attack.getSixth());
            }
            return new Attack(data);                  // a zero attack factor and modifier.
        }

        public int getEndurance(final Performance performance) {
            return performance.getEndurance();
        }

        public int getFerryDistance(final Attack land, final Attack naval, final Performance performance) {
            return performance.getFerryDistance() * STRIPPED_DOWN_FACTOR;
        }

        public int getRadius(final Attack land, final Attack naval, final Performance performance) {
            return performance.getRadius() * STRIPPED_DOWN_FACTOR;
        }
    };

    private final String value;
    @Getter private final Integer priority;

    private static final double DROP_TANK_FACTOR = 1.5;        // Drop tanks increase range by 1.5 times.
    private static final int LEAN_ENGINE_FACTOR = 2;           // Much reduced payload to extend range and endurance.
    private static final int REDUCED_FACTOR = 2;               // Reduced payload to extend range.
    private static final int SEARCH_FACTOR = 2;                // Used when on searches.
    private static final int STRIPPED_DOWN_FACTOR = 3;         // Stripping down the fighter of ordinance and adding extra full increases the range by 3.
    private static final int SEARCH_MODIFIER = 2;              // Squadron configured for search has less ordinance and more fuel. This is the increase in range.
    private static final int ORDINANCE_PAYLOAD_THRESHOLD = 2;  // Additional range threshold for SEARCH and REDUCED_ORDINANCE configurations.

    /**
     * Modify the given attack by the squadron configuration parameters.
     *
     * @param type The type of attack.
     * @param attack The actual attack.
     * @return The modified attack of the squadron.
     */
    public abstract Attack getAttack(AttackType type, Attack attack);

    /**
     * Modify the given radius by the squadron configuration parameters.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor. May be naval warship or naval transport.
     *              It does not matter which.
     * @param performance The squadron's performance which contains the squadron's radius.
     * @return The modified combat radius of the squadron.
     */
    public abstract int getRadius(Attack land, Attack naval, Performance performance);

    /**
     * Modify the ferry distance by the squadron configuration parameters.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor. May be naval warship or naval transport.
     *              It does not matter which.
     * @param performance The squadron's performance which contains the squadron's ferry distance.
     * @return The modified ferry distance of the squadron.
     */
    public abstract int getFerryDistance(Attack land, Attack naval, Performance performance);
    public abstract int getEndurance(Performance performance);

    /**
     * Constructor.
     *
     * @param value The String value of this enum.
     * @param priority The priority of the configuration.
     */
    SquadronConfig(final String value, final int priority) {
        this.value = value;
        this.priority = priority;
    }

    /**
     * The String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    public String toString() {
        return value;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     * <p>
     * The implementor must ensure that {@code sgn(compare(x, y)) ==
     * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
     * implies that {@code compare(x, y)} must throw an exception if and only
     * if {@code compare(y, x)} throws an exception.)<p>
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
     * {@code compare(x, z)>0}.<p>
     * <p>
     * Finally, the implementor must ensure that {@code compare(x, y)==0}
     * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
     * {@code z}.<p>
     * <p>
     * It is generally the case, but <i>not</i> strictly required that
     * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."<p>
     * <p>
     * In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(final SquadronConfig o1, final SquadronConfig o2) {
        return o1.priority.compareTo(o2.priority);
    }

    /**
     * Get a stream of this enum values.
     *
     * @return Stream of this enum values.
     */
    public static Stream<SquadronConfig> stream() {
        return Stream.of(values());
    }

    /**
     * Get the search range modifier for this aircraft. If the aircraft has extra fuel
     * capacity, then the range modifier is increased by a set amount.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor.
     * @return The search range modifier for this aircraft.
     */
    int getSearchModifier(final Attack land, final Attack naval) {
        return SEARCH_MODIFIER + (hasExtraFuelCapacity(land, naval) ? SEARCH_MODIFIER : 0);
    }

    /**
     * Get the reduced payload range modifier for this aircraft. If the aircraft has extra fuel
     * capacity, then the range modifier is increased by a set amount.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor.
     * @return The reduced payload range modifier for this aircraft.
     */
    int getReducedPayloadModifier(final Attack land, final Attack naval) {
        return hasExtraFuelCapacity(land, naval) ? SEARCH_MODIFIER : 0;
    }

    /**
     * Determine if this aircraft has extra capacity for fuel if its naval/land payload is reduced.
     * The aircraft must have an ordinance capacity great enough to carry extra fuel. This is
     * determined by the naval/land attack factor of the aircraft. If one of these factors is
     * greater than the defined threshold, then this aircraft can carry extra fuel in place of
     * ordinance to extend its range.
     *
     * @param land The squadron's land attack factor.
     * @param naval The squadron's naval attack factor.
     * @return True if this aircraft has extra fuel capacity. False otherwise.
     */
    private boolean hasExtraFuelCapacity(final Attack land, final Attack naval) {
        return land.getFull() >= ORDINANCE_PAYLOAD_THRESHOLD
                || naval.getFull() >= ORDINANCE_PAYLOAD_THRESHOLD;
    }
}
