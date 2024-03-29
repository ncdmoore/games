package engima.waratsea.model.aircraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Represents the type of landing an aircraft is capable of performing.
 *
 * CARRIER - lands and takes off from aircraft carriers and land airfields.
 * LAND - lands and takes off only from land airfields.
 * SEAPLANE - lands and takes off from the sea.
 * FLOATPLANE - lands and takes off from the sea and may be stored on capital ships.
 */
@RequiredArgsConstructor
public enum LandingType {
    @SerializedName(value = "CARRIER", alternate = {"Carrier", "carrier"})
    CARRIER("Carrier"),

    @SerializedName(value = "LAND", alternate = {"Land", "land"})
    LAND("Land"),

    @SerializedName(value = "SEAPLANE", alternate = {"Seaplane", "seaplane"})
    SEAPLANE("Seaplane"),

    @SerializedName(value = "FLOATPLANE", alternate = {"Floatplane", "floatplane"})
    FLOATPLANE("Float plane");

    @Getter
    private final String value;

    /**
     * Get the String representation.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Get a stream of this enum's values.
     *
     * @return A Stream of this enum's values.
     */
    public static Stream<LandingType> stream() {
        return Stream.of(LandingType.values());
    }
}
