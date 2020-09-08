package engima.waratsea.model.squadron;

import lombok.Getter;

import java.util.Comparator;

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
    NONE("Basic", 1),                      // No special configuration.
    DROP_TANKS("Drop Tanks", 2),           // The squadron is equipped with drop tanks - this extends the squadron's range. Exclusive to fighters.
    LEAN_ENGINE("Long Range", 3),          // The squadron is both equipped with extra internal fuel and is running very lean to extend its range. Any payload is reduced.
    REDUCED_PAYLOAD("Reduced Payload", 4), // The squadron has a reduced payload to allow for extra fuel.
    SEARCH("Search", 5),                   // The squadron is equipped for a search. No ordinance and extra fuel.
    STRIPPED_DOWN("Stripped Down", 6);     // The squadron is stripped of ordinance and is loaded with extra fuel. This extends the squadron's range. Exclusive to fighters.

    private final String value;
    @Getter private final Integer priority;

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
}
