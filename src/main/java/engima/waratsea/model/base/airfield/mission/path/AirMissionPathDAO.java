package engima.waratsea.model.base.airfield.mission.path;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.path.data.AirMissionPathData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class AirMissionPathDAO {
    private final Map<AirMissionType, Function<AirMissionPathData, AirMissionPath>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The air mission path factory.
     */
    @Inject
    public AirMissionPathDAO(final AirMissionPathFactory factory) {
        factoryMap.put(AirMissionType.FERRY, factory::createOneWay);
        factoryMap.put(AirMissionType.LAND_STRIKE, factory::createRoundTrip);
        factoryMap.put(AirMissionType.NAVAL_PORT_STRIKE, factory::createRoundTrip);
        factoryMap.put(AirMissionType.SWEEP_AIRFIELD, factory::createRoundTrip);
        factoryMap.put(AirMissionType.SWEEP_PORT, factory::createRoundTrip);
    }

    /**
     * Load the air mission path from the air mission path data.
     *
     * @param data Air mission path data read in from a JSON file.
     * @return A newly created air mission path.
     */
    public AirMissionPath load(final AirMissionPathData data) {
        return factoryMap.get(data.getType()).apply(data);
    }
}
