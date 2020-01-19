package engima.waratsea.model.base.airfield.mission;

import java.util.HashMap;
import java.util.Map;

public enum MissionType {

    FERRY("Ferry"),
    LAND_STRIKE("Airfield Strike"),
    NAVAL_PORT_STRIKE("Port Strike"),
    NAVAL_TASK_FORCE_STRIKE("Task Force Strike"),
    SWEEP_AIRFIELD("Airfield Sweep"),
    SWEEP_PORT("Port Sweep");

    private String value;

    private static Map<Class<?>, MissionType> typeMap = new HashMap<>();

    static {
        typeMap.put(Ferry.class, FERRY);
        typeMap.put(LandStrike.class, LAND_STRIKE);
        typeMap.put(NavalPortStrike.class, NAVAL_PORT_STRIKE);
        typeMap.put(NavalTaskForceStrike.class, NAVAL_TASK_FORCE_STRIKE);
        typeMap.put(SweepAirfield.class, SWEEP_AIRFIELD);
        typeMap.put(SweepPort.class, SWEEP_PORT);
    }

    /**
     * Constructor.
     *
     * @param value The String value of the enum.
     */
    MissionType(final String value) {
        this.value = value;
    }

    /**
     * Get the enum mission type given a mission object.
     *
     * @param mission A air base mission.
     * @return The mission type enum of the given mission object.
     */
    public static MissionType getType(final Mission mission) {
        return typeMap.get(mission.getClass());
    }

    /**
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    public String toString() {
        return value;
    }
}
