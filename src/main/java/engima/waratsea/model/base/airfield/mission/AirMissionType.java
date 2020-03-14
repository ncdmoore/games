package engima.waratsea.model.base.airfield.mission;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents an air mission type. These are the types of mission a squadron may perform. Though
 * not all mission may be performed by all aircraft types.
 *
 * Squadrons have roles within a mission. For example on a Naval Strike mission a squadron may participate
 * in the actual strike itself, i.e., attack a ship, or it may serve as an escort.
 */
public enum AirMissionType {

    FERRY("Ferry", "Ferry", Collections.singletonList(MissionRole.MAIN)),
    LAND_STRIKE("Airfield Strike", "Strike", Arrays.asList(MissionRole.MAIN, MissionRole.ESCORT)),
    NAVAL_PORT_STRIKE("Port Strike", "Strike", Arrays.asList(MissionRole.MAIN, MissionRole.ESCORT)),
    NAVAL_TASK_FORCE_STRIKE("Task Force Strike", "Strike", Arrays.asList(MissionRole.MAIN, MissionRole.ESCORT)),
    SWEEP_AIRFIELD("Airfield Sweep", "Sweep", Collections.singletonList(MissionRole.MAIN)),
    SWEEP_PORT("Port Sweep", "Sweep", Collections.singletonList(MissionRole.MAIN));

    private final String value;

    @Getter
    private final String title;

    @Getter
    private List<MissionRole> roles;

    private static Map<Class<?>, AirMissionType> typeMap = new HashMap<>();

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
     * @param title The mission type's title.
     * @param roles The available squadron roles for the type of mission.
     */
    AirMissionType(final String value, final String title, final List<MissionRole> roles) {
        this.value = value;
        this.title = title;
        this.roles = roles;
    }

    /**
     * Get the enum mission type given a mission object.
     *
     * @param mission A air base mission.
     * @return The mission type enum of the given mission object.
     */
    public static AirMissionType getType(final AirMission mission) {
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
