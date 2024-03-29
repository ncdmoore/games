package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Defines all the aircraft types in the game.
 */
@AllArgsConstructor
public enum AircraftType implements Comparator<AircraftType> {
    @SerializedName(value = "FIGHTER", alternate = {"Fighter", "fighter"})
    FIGHTER("Fighter", "Fighter", "F", 1),

    @SerializedName(value = "BOMBER", alternate = {"Bomber", "bomber"})
    BOMBER("Bomber", "Bomber", "B", 2),

    @SerializedName(value = "DIVE_BOMBER", alternate = {"Dive_Bomber", "dive_bomber", "Dive Bomber", "dive bomber"})
    DIVE_BOMBER("Dive Bomber", "Dive Bomber", "DB", 3),

    @SerializedName(value = "TORPEDO_BOMBER", alternate = {"Torpedo_Bomber", "torpedo_bomber", "Torpedo Bomber", "torpedo bomber"})
    TORPEDO_BOMBER("Torpedo Bomber", "Torp. Bomber", "TB", 4),

    @SerializedName(value = "RECONNAISSANCE", alternate = {"Reconnaissance", "reconnaissance"})
    RECONNAISSANCE("Recon", "Recon", "R", 5);

    private static final List<AircraftType> VALUES = Arrays.asList(AircraftType.values());

    private final String value;
    @Getter private final String abbreviated;
    @Getter private final String designation;
    private final Integer order;  // Enum sort order.

    /**
     * This is used to loop through the enum values. This returns the next value
     * in the list based off the current value. If the end of the list is reached
     * then the head of the list is returned.
     *
     * @return The next reference in the enum values list.
     */
    public AircraftType next() {
        int index = VALUES.indexOf(this);

        if (index + 1 == VALUES.size()) {
            index = 0;
        } else {
            index++;
        }

        return VALUES.get(index);
    }

    /**
     * Return the string representation of the enum.
     *
     * @return The string value of the ShipType enum.
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
    public int compare(final AircraftType o1, final AircraftType o2) {
        return o1.order.compareTo(o2.order);
    }

    /**
     * Get a Stream of all the aircraft types.
     *
     * @return A Stream of the aircraft types.
     */
    public static Stream<AircraftType> stream() {
        return Stream.of(AircraftType.values());
    }
}
