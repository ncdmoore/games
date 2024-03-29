package engima.waratsea.model.game;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The game nations.
 */
@RequiredArgsConstructor
public enum Nation implements Comparator<Nation> {
    @SerializedName(value = "AUSTRALIAN", alternate = {"Australian", "australian"})
    AUSTRALIAN("Australian", "Australian", "HMAS"),

    @SerializedName(value = "BRITISH", alternate = {"British", "british"})
    BRITISH("British", "British", "HMS"),

    @SerializedName(value = "FINNISH", alternate = {"Finnish", "finnish"})
    FINNISH("Finnish", "Finnish", "FN"),

    @SerializedName(value = "FRENCH", alternate = {"French", "french"})
    FRENCH("French", "French", ""),

    @SerializedName(value = "GERMAN", alternate = {"German", "german"})
    GERMAN("German", "German", "KMS"),

    @SerializedName(value = "GREEK", alternate = {"Greek", "greek"})
    GREEK("Greek", "Greek", "HS"),

    @SerializedName(value = "ITALIAN", alternate = {"Italian", "italian"})
    ITALIAN("Italian", "Italian", "RN"),

    @SerializedName(value = "JAPANESE", alternate = {"Japanese", "japanese"})
    JAPANESE("Japanese", "Japanese", "IJN"),

    @SerializedName(value = "POLISH", alternate = {"Polish", "polish"})
    POLISH("Polish", "Polish", "ORP"),

    @SerializedName(value = "UNITED_STATES", alternate = {"UNITED STATES", "United_States", "united_states", "United States", "united states"})
    UNITED_STATES("United States", "United-States", "USS"),

    @SerializedName(value = "USSR", alternate = {"Ussr", "ussr"})
    USSR("USSR", "ussr", "CCCP");

    private final String value;

    @Getter
    private final String fileName;

    @Getter
    private final String shipPrefix;

    @Getter
    @Setter
    private boolean squadronsPresent;

    private static final Map<String, Nation> VALUE_MAP = Map.of(
        "Australian", Nation.AUSTRALIAN,
        "British", Nation.BRITISH,
        "French", Nation.FRENCH,
        "German", Nation.GERMAN,
        "Greek", Nation.GREEK,
        "Italian", Nation.ITALIAN,
        "Japanese", Nation.JAPANESE,
        "Polish", Nation.POLISH,
        "United States", Nation.UNITED_STATES
    );

    /**
     * Get the enum given the value.
     *
     * @param value the enum value.
     * @return The corresponding enum to the given value.
     */
    public static Nation get(final String value) {
        return VALUE_MAP.get(value);
    }

    /**
     * The string representation of the nation.
     *
     * @return The string representation of the nation.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get the lower case String representation of the nation.
     *
     * @return The lower case String representation of the nation.
     */
    public String toLower() {
        return toString().toLowerCase();
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
    public int compare(final Nation o1, final Nation o2) {
        return o1.value.compareTo(o2.value);
    }

    /**
     * Get a stream of this enum's values.
     *
     * @return A stream of this enum's values.
     */
    public static Stream<Nation> stream() {
        return Stream.of(Nation.values());
    }
}
