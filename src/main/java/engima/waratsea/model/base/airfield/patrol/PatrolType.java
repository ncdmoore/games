package engima.waratsea.model.base.airfield.patrol;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
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

    private static final Map<Class<?>, String> TITLE_MAP = new HashMap<>();
    private static final Map<Class<?>, PatrolType> TYPE_MAP = new HashMap<>();

    static {
        TITLE_MAP.put(AswPatrol.class, "ASW");
        TITLE_MAP.put(CapPatrol.class, "CAP");
        TITLE_MAP.put(SearchPatrol.class, "Search");

        TYPE_MAP.put(AswPatrol.class, ASW);
        TYPE_MAP.put(CapPatrol.class, CAP);
        TYPE_MAP.put(SearchPatrol.class, SEARCH);
    }

    /**
     * Get the patrol's title from its class.
     *
     * @param patrol A patrol.
     * @return The corresponding title.
     */
    public static String getTitle(final Patrol patrol) {
        return TITLE_MAP.get(patrol.getClass());
    }

    /**
     * Get the patrol's type from its class.
     *
     * @param patrol A patrol.
     * @return The corresponding enum.
     */
    public static PatrolType getType(final Patrol patrol) {
        return TYPE_MAP.get(patrol.getClass());
    }

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
