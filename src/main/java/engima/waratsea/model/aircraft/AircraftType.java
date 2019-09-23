package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.Comparator;

/**
 * Defines all the aircraft types in the game.
 */
public enum AircraftType implements Comparator<AircraftType> {
    @SerializedName(value = "FIGHTER", alternate = {"Fighter", "fighter"})
    FIGHTER("Fighter", AircraftBaseType.FIGHTER, "F", 1),

    @SerializedName(value = "BOMBER", alternate = {"Bomber", "bomber"})
    BOMBER("Bomber", AircraftBaseType.BOMBER, "B", 2),

    @SerializedName(value = "POOR_NAVAL_BOMBER", alternate = {"Poor_Naval_Bomber", "poor_naval_bomber"})
    POOR_NAVAL_BOMBER("Bomber", AircraftBaseType.BOMBER, "B", 2),

    @SerializedName(value = "DIVE_BOMBER", alternate = {"Dive_Bomber", "dive_bomber", "Dive Bomber", "dive bomber"})
    DIVE_BOMBER("Dive Bomber", AircraftBaseType.BOMBER, "DB", 3),

    @SerializedName(value = "TORPEDO_BOMBER", alternate = {"Torpedo_Bomber", "torpedo_bomber", "Torpedo Bomber", "torpedo bomber"})
    TORPEDO_BOMBER("Torpedo Bomber", AircraftBaseType.BOMBER, "TB", 4),

    @SerializedName(value = "RECONNAISSANCE", alternate = {"Reconnaissance", "reconnaissance"})
    RECONNAISSANCE("Reconnaissance", AircraftBaseType.RECON, "R", 5);

    private String value;

    @Getter
    private AircraftBaseType baseType;

    @Getter
    private String designation;

    private Integer order;

    /**
     * Constructor.
     *
     * @param value The string value of the enum.
     * @param baseType The base aircraft baseType.
     * @param designation The aircraft designation. F for fighter, B for bomber, etc.
     * @param order The sort order of the enum.
     */
    AircraftType(final String value, final AircraftBaseType baseType, final String designation, final int order) {
        this.value = value;
        this.baseType = baseType;
        this.designation = designation;
        this.order = order;
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
}
