package engima.waratsea.model.squadron;

import engima.waratsea.model.base.airfield.patrol.AswPatrol;
import engima.waratsea.model.base.airfield.patrol.CapPatrol;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.SearchPatrol;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum PatrolType {
    ASW("ASW"),
    CAP("CAP"),
    SEARCH("Search");

    private static Map<Class<?>, String> titleMap = new HashMap<>();

    static {
        titleMap.put(AswPatrol.class, "ASW");
        titleMap.put(CapPatrol.class, "CAP");
        titleMap.put(SearchPatrol.class, "Search");
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
