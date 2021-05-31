package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Airbase;
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
    private final Map<TargetType, Function<TargetData, Target>> factoryMap = new HashMap<>();
    private final Map<TargetType, Map<String, Target>> cache = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param factory The target factory.
     */
    @Inject
    public TargetDAO(final TargetFactory factory) {
        factoryMap.put(TargetType.ENEMY_AIRFIELD, factory::createEnemyAirfieldTarget);
        factoryMap.put(TargetType.FRIENDLY_AIRBASE, factory::createFriendlyAirfieldTarget);
        factoryMap.put(TargetType.ENEMY_PORT, factory::createEnemyPortTarget);
        factoryMap.put(TargetType.FRIENDLY_PORT, factory::createFriendlyPortTarget);
        factoryMap.put(TargetType.ENEMY_TASK_FORCE, factory::createEnemyTaskForceTarget);
        factoryMap.put(TargetType.FRIENDLY_TASK_FORCE, factory::createFriendlyTaskForceTarget);
        factoryMap.put(TargetType.SEA_GRID, factory::createSeaGrid);
        factoryMap.put(TargetType.LAND_GRID, factory::createLandGrid);

        TargetType
                .stream()
                .forEach(type -> cache.put(type, new HashMap<>()));
    }

    /**
     * Initialize the cache.
     */
    public void init() {
        cache.values().forEach(Map::clear);
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
     * Get a friendly airbase target.
     *
     * @param airbase The friendly airbase.
     * @return A friendly airfield target that corresponds to the given airbase.
     */
    public Target getFriendlyAirbaseTarget(final Airbase airbase) {
        if (cache.get(TargetType.FRIENDLY_AIRBASE).containsKey(airbase.getName())) {
            return cache.get(TargetType.FRIENDLY_AIRBASE).get(airbase.getName());
        }

        TargetData data = new TargetData();
        data.setSide(airbase.getSide());
        data.setName(airbase.getName());
        data.setType(TargetType.FRIENDLY_AIRBASE);

        Target target = load(data);
        cache.get(TargetType.FRIENDLY_AIRBASE).put(airbase.getName(), target);
        return target;
    }

    /**
     * Get an enemy airfield target.
     *
     * @param airfield The enemy airfield.
     * @return An enemy airfield target that corresponds to the given airfield.
     */
    public Target getEnemyAirfieldTarget(final Airfield airfield) {
        if (cache.get(TargetType.ENEMY_AIRFIELD).containsKey(airfield.getName())) {
            return cache.get(TargetType.ENEMY_AIRFIELD).get(airfield.getName());
        }

        TargetData data = new TargetData();
        data.setSide(airfield.getSide().opposite());
        data.setName(airfield.getName());
        data.setType(TargetType.ENEMY_AIRFIELD);

        Target target = load(data);
        cache.get(TargetType.ENEMY_AIRFIELD).put(airfield.getName(), target);
        return target;
    }

    /**
     * Get an enemy port target.
     *
     * @param port The enemy port.
     * @return An enemy port target that corresponds to the given port.
     */
    public Target getEnemyPortTarget(final Port port) {
        if (cache.get(TargetType.ENEMY_PORT).containsKey(port.getName())) {
            return cache.get(TargetType.ENEMY_PORT).get(port.getName());
        }

        TargetData data = new TargetData();
        data.setSide(port.getSide().opposite());
        data.setName(port.getName());
        data.setType(TargetType.ENEMY_PORT);

        Target target = load(data);
        cache.get(TargetType.ENEMY_PORT).put(port.getName(), target);
        return target;
    }

    /**
     * Get a friendly task force target.
     *
     * @param taskForce The friendly task force.
     * @return A friendly task force target that corresponds to the given task force.
     */
    public Target getFriendlyTaskForceTarget(final TaskForce taskForce) {
        if (cache.get(TargetType.FRIENDLY_TASK_FORCE).containsKey(taskForce.getName())) {
            return cache.get(TargetType.FRIENDLY_TASK_FORCE).get(taskForce.getName());
        }

        TargetData data = new TargetData();
        data.setSide(taskForce.getSide());
        data.setName(taskForce.getName());
        data.setType(TargetType.FRIENDLY_TASK_FORCE);

        Target target = load(data);
        cache.get(TargetType.FRIENDLY_TASK_FORCE).put(taskForce.getName(), target);
        return target;
    }

    /**
     * Get an enemy task force target.
     *
     * @param taskForce The enemy task force.
     * @return An enemy task force target that corresponds to the given task force.
     */
    public Target getEnemyTaskForceTarget(final TaskForce taskForce) {
        if (cache.get(TargetType.ENEMY_TASK_FORCE).containsKey(taskForce.getName())) {
            return cache.get(TargetType.ENEMY_TASK_FORCE).get(taskForce.getName());
        }

        TargetData data = new TargetData();
        data.setSide(taskForce.getSide().opposite());
        data.setName(taskForce.getName());
        data.setType(TargetType.ENEMY_TASK_FORCE);

        Target target = load(data);
        cache.get(TargetType.ENEMY_TASK_FORCE).put(taskForce.getName(), target);
        return target;
    }
}
