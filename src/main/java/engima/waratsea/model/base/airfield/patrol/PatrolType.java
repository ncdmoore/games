package engima.waratsea.model.base.airfield.patrol;

import lombok.Getter;

import java.util.stream.Stream;


/**
 * Represents the type of patrol.
 *
 * Note, do not override the toString method as this can cause issues with gson.
 *
 * A patrol can be virtual if it is the result of a mission, such as a distant CAP mission over a task force.
 * A distant CAP mission creates a "virtual" CAP patrol over the task force.
 */
public enum PatrolType {
    ASW("ASW", false),
    CAP("CAP", true),
    SEARCH("Search", false);

    /**
     * Get a stream of all of the patrol types.
     *
     * @return A Stream of all of the patrol types.
     */
    public static Stream<PatrolType> stream() {
        return Stream.of(values());
    }

    /**
     * Get a stream of all patrol types that can be virtual.
     *
     * @return S Stream of virtual patrol types.
     */
    public static Stream<PatrolType> virtualStream() {
        return Stream
                .of(values())
                .filter(PatrolType::isVirtual);
    }

    @Getter private final String value;
    @Getter private final boolean virtual;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     * @param canBeVirtual Indicates if this patrol type may be a virtual patrol.
     */
    PatrolType(final String value, final boolean canBeVirtual) {
        this.value = value;
        this.virtual = canBeVirtual;
    }

}
