package engima.waratsea.model.base.airfield.mission;

import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
    private final List<MissionRole> roles;

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
     * Get the String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    public String toString() {
        return value;
    }

    /**
     * Get the String lower case representation of this enum.
     *
     * @return The String lower case representation of this enum.
     */
    public String toLower() {
        return toString().toLowerCase();
    }

    /**
     * Get a stream of this enum's values.
     *
     * @return A stream of this enum's values.
     */
    public static Stream<AirMissionType> stream() {
        return Stream.of(AirMissionType.values());
    }
}
