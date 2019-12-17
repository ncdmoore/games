package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.target.data.TargetData;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The target data access object. Provices accesst to all target objects.
 */
@Singleton
@Slf4j
public class TargetDAO {

    private Map<TargetType, Function<TargetData, Target>> factoryMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The target factory.
     */
    @Inject
    public TargetDAO(final TargetFactory factory) {
        factoryMap.put(TargetType.ENEMY_AIRFIELD, factory::createEnemyAirfieldTarget);
        factoryMap.put(TargetType.FRIENDLY_AIRFIELD, factory::createFriendlyAirfieldTarget);
        factoryMap.put(TargetType.ENEMY_PORT, factory::createEnemyPortTarget);
        factoryMap.put(TargetType.FRIENDLY_PORT, factory::createFriendlyPortTarget);
        factoryMap.put(TargetType.SEA_GRID, factory::createSeaGrid);
    }

    /**
     * Load the target.
     *
     * @param data The target data.
     * @return A target.
     */
    public Target load(final TargetData data) {
        return factoryMap.get(data.getType()).apply(data);
    }
}
