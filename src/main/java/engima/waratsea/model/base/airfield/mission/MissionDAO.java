package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.mission.data.MissionData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class MissionDAO {
    private final Map<AirMissionType, Function<MissionData, AirMission>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The mission factory.
     */
    @Inject
    public MissionDAO(final MissionFactory factory) {
        factoryMap.put(AirMissionType.FERRY, factory::createFerry);
        factoryMap.put(AirMissionType.LAND_STRIKE, factory::createLandStrike);
        factoryMap.put(AirMissionType.NAVAL_PORT_STRIKE, factory::createNavalPortStrike);
        factoryMap.put(AirMissionType.SWEEP_AIRFIELD, factory::createSweepAirfield);
        factoryMap.put(AirMissionType.SWEEP_PORT, factory::createSweepPort);
    }

    /**
     * Load the mission from the mission data.
     *
     * @param data Mission data read in from a JSON file.
     * @return A newly created mission.
     */
    public AirMission load(final MissionData data) {
        return factoryMap.get(data.getType()).apply(data);
    }
}
