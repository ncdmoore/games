package engima.waratsea.model.taskForce.mission;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.taskForce.mission.data.MissionData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Mission data access object.
 *
 */
@Singleton
@Slf4j
public class MissionDAO {
    private Map<MissionType, Function<MissionData, SeaMission>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The Mission factory.
     */
    @Inject
    public MissionDAO(final MissionFactory factory) {
        factoryMap.put(MissionType.AIR_RAID, factory::createAirRaid);
        factoryMap.put(MissionType.BOMBARDMENT, factory::createBombardment);
        factoryMap.put(MissionType.ESCORT, factory::createEscort);
        factoryMap.put(MissionType.FERRY, factory::createFerry);
        factoryMap.put(MissionType.FERRY_AIRCRAFT, factory::createFerryAircraft);
        factoryMap.put(MissionType.INTERCEPT, factory::createIntercept);
        factoryMap.put(MissionType.INVASION, factory::createInvasion);
        factoryMap.put(MissionType.MINELAYING, factory::createMinelaying);
        factoryMap.put(MissionType.PATROL, factory::createPatrol);
        factoryMap.put(MissionType.TRANSPORT, factory::createTransport);

    }

    /**
     * Load the mission.
     *
     * @param data The mission data.
     * @return The mission.
     */
    public SeaMission load(final MissionData data) {
        return factoryMap.get(data.getType()).apply(data);
    }
}
