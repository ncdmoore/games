package engima.waratsea.model.base.airfield.patrol;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum PatrolType {
    ASW("ASW"),
    CAP("CAP"),
    SEARCH("Search");

    private static Map<Class<?>, String> titleMap = new HashMap<>();
    private static Map<Class<?>, PatrolType> typeMap = new HashMap<>();

    static {
        titleMap.put(AswPatrol.class, "ASW");
        titleMap.put(CapPatrol.class, "CAP");
        titleMap.put(SearchPatrol.class, "Search");
    }

    static {
        typeMap.put(AswPatrol.class, ASW);
        typeMap.put(CapPatrol.class, CAP);
        typeMap.put(SearchPatrol.class, SEARCH);
    }

    /**
     * Get the patrol's title from its class.
     *
     * @param patrol A patrol.
     * @return The corresponding title.
     */
    public static String getTitle(final Patrol patrol) {
        return titleMap.get(patrol.getClass());
    }

    /**
     * Get the patrol's type from its class.
     *
     * @param patrol A patrol.
     * @return The corresponding enum.
     */
    public static PatrolType getType(final Patrol patrol) {
        return typeMap.get(patrol.getClass());
    }

    @Getter
    private String value;

    /**
     * The constructor.
     *
     * @param value The String representation of this enum.
     */
    PatrolType(final String value) {
        this.value = value;
    }
}