package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The target data access object. Provides access to all target objects.
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
        factoryMap.put(TargetType.ENEMY_TASK_FORCE, factory::createEnemyTaskForceTarget);
        factoryMap.put(TargetType.FRIENDLY_TASK_FORCE, factory::createFriendlyTaskForceTarget);
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

    /**
     * Get a friendly airfield target.
     *
     * @param airfield The friendly airfield.
     * @return A friendly airfield target that corresponds to the given airfield.
     */
    public Target getFriendlyAirfieldTarget(final Airfield airfield) {
        TargetData data = new TargetData();
        data.setSide(airfield.getSide());
        data.setName(airfield.getName());
        data.setType(TargetType.FRIENDLY_AIRFIELD);

        return load(data);
    }

    /**
     * Get an enemy airfield target.
     *
     * @param airfield The enemy airfield.
     * @return An enemy airfield target that corresponds to the given airfield.
     */
    public Target getEnemyAirfieldTarget(final Airfield airfield) {
        TargetData data = new TargetData();
        data.setSide(airfield.getSide().opposite());
        data.setName(airfield.getName());
        data.setType(TargetType.ENEMY_AIRFIELD);

        return load(data);
    }

    /**
     * Get an enemy port target.
     *
     * @param port The enemy port.
     * @return An enemy port target that corresponds to the given port.
     */
    public Target getEnemyPortTarget(final Port port) {
        TargetData data = new TargetData();
        data.setSide(port.getSide().opposite());
        data.setName(port.getName());
        data.setType(TargetType.ENEMY_PORT);

        return load(data);
    }

    /**
     * Get an enemy task force target.
     *
     * @param taskForce The enemy task force.
     * @return An enemy task force target that corresponds to the given task force.
     */
    public Target getEnemyTaskForceTarget(final TaskForce taskForce) {
        TargetData data = new TargetData();
        data.setSide(taskForce.getSide().opposite());
        data.setName(taskForce.getName());
        data.setType(TargetType.ENEMY_TASK_FORCE);

        return load(data);
    }
}
