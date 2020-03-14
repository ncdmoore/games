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
    private Map<SeaMissionType, Function<MissionData, SeaMission>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The Mission factory.
     */
    @Inject
    public MissionDAO(final MissionFactory factory) {
        factoryMap.put(SeaMissionType.AIR_RAID, factory::createAirRaid);
        factoryMap.put(SeaMissionType.BOMBARDMENT, factory::createBombardment);
        factoryMap.put(SeaMissionType.ESCORT, factory::createEscort);
        factoryMap.put(SeaMissionType.FERRY, factory::createFerry);
        factoryMap.put(SeaMissionType.FERRY_AIRCRAFT, factory::createFerryAircraft);
        factoryMap.put(SeaMissionType.INTERCEPT, factory::createIntercept);
        factoryMap.put(SeaMissionType.INVASION, factory::createInvasion);
        factoryMap.put(SeaMissionType.MINELAYING, factory::createMinelaying);
        factoryMap.put(SeaMissionType.PATROL, factory::createPatrol);
        factoryMap.put(SeaMissionType.TRANSPORT, factory::createTransport);

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
