package engima.waratsea.model.base.airfield.mission;

import com.google.inject.name.Named;
import engima.waratsea.model.base.airfield.mission.data.MissionData;

public interface MissionFactory {

    /**
     * Create an airbase ferry mission.
     *
     * @param data The mission data.
     * @return A ferry mission initialized with the given data.
     */
    @Named("ferry")
    Mission createFerry(MissionData data);

    /**
     * Create an airbase land strike mission.
     *
     * @param data The mission data.
     * @return A land strike mission initialized with the given data.
     */
    @Named("landStrike")
    Mission createLandStrike(MissionData data);

    /**
     * Create an airbase naval strike mission.
     *
     * @param data The mission data.
     * @return A naval strike mission initialized with the given data.
     */
    @Named("navalPortStrike")
    Mission createNavalPortStrike(MissionData data);

    /**
     * Create an airbase fighter sweep mission over an enemy airfield.
     *
     * @param data The mission data.
     * @return A fighter sweep mission initialized with the given data.
     */
    @Named("sweepAirfield")
    Mission createSweepAirfield(MissionData data);

    /**
     * Create an airbase fighter sweep mission over an enemy port.
     *
     * @param data The mission data.
     * @return A fighter sweep mission initialized with the given data.
     */
    @Named("sweepPort")
    Mission createSweepPort(MissionData data);
}
