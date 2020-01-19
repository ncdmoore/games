package engima.waratsea.model.base.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.mission.data.MissionData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Singleton
public class MissionDAO {
    private Map<MissionType, Function<MissionData, Mission>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The mission factory.
     */
    @Inject
    public MissionDAO(final MissionFactory factory) {
        factoryMap.put(MissionType.FERRY, factory::createFerry);
        factoryMap.put(MissionType.LAND_STRIKE, factory::createLandStrike);
        factoryMap.put(MissionType.NAVAL_PORT_STRIKE, factory::createNavalPortStrike);
        factoryMap.put(MissionType.SWEEP_AIRFIELD, factory::createSweepAirfield);
        factoryMap.put(MissionType.SWEEP_PORT, factory::createSweepPort);
    }

    /**
     * Load the mission from the mission data.
     *
     * @param data Mission data read in from a JSON file.
     * @return A newly created mission.
     */
    public Mission load(final MissionData data) {
        return factoryMap.get(data.getType()).apply(data);
    }
}
