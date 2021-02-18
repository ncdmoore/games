package engima.waratsea.model.base.airfield.patrol;

import lombok.Getter;

import java.util.stream.Stream;


/**
 * Represents the type of patrol.
 *
 * Note, do not override the toString method as this can cause issues with gson.
 */
public enum PatrolType {
    ASW("ASW"),
    CAP("CAP"),
    SEARCH("Search");

    /**
     * Get a stream of all of the patrol types.
     *
     * @return A Stream of all of the patrol types.
     */
    public static Stream<PatrolType> stream() {
        return Stream.of(values());
    }

    @Getter
    private final String value;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     */
    PatrolType(final String value) {
        this.value = value;
    }
}
