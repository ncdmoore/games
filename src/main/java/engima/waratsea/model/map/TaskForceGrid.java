package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.AirbaseGroup;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class represents a task force grid on the game map.
 * Note, that a task force grid moves as the task force moves across the map.
 *
 * As long as the task force exists this task force grid will exist.
 *
 * If a two task forces are combined then one of the task force grids is removed.
 */
@Slf4j
public class TaskForceGrid implements MarkerGrid {

    private final Provider<GameMap> gameMapProvider;

    @Getter private Side side;                                            // The side of the task force.
    @Getter private String reference;                                     // The map reference location of this task force grid.
    @Getter private final List<TaskForce> taskForces = new ArrayList<>(); // The task forces present at this grid.

    /**
     * The constructor called by guice.
     *
     * @param gameMapProvider Provides the game map.
     */
    @Inject
    public TaskForceGrid(final Provider<GameMap> gameMapProvider) {
        this.gameMapProvider = gameMapProvider;
    }

    /**
     * Initialize from the given task taskForce.
     *
     * @param taskForce The task force.
     * @return This task taskForce grid.
     */
    public TaskForceGrid init(final TaskForce taskForce) {
        taskForces.add(taskForce);
        side = taskForce.getSide();
        reference = taskForce.getReference();
        return this;
    }

    /**
     * Add a task force to this grid. Recall a grid may contain multiple task forces.
     *
     * @param taskForce The task force to add.
     */
    public void add(final TaskForce taskForce) {
        taskForces.add(taskForce);
    }

    /**
     * Remove the given task force from this grid. Recall a grid may contain multiple task forces.
     * If the task force is not really on this grid then nothing happens.
     *
     * @param taskForce The task force to remove.
     */
    public void remove(final TaskForce taskForce) {
        taskForces.remove(taskForce);
    }

    /**
     * Determine if this grid contains any task forces. If no task forces are present then it can be removed
     * from the game map.
     *
     * @return True if this grid contains task forces. False otherwise.
     */
    public boolean notEmpty() {
        return !taskForces.isEmpty();
    }

    /**
     * Determine if the given task force is located at this grid.
     *
     * @param taskForce The task force examined to determine if it is located at this grid.
     * @return True if the given task force is located at this grid.
     */
    public boolean matches(final TaskForce taskForce) {
        return getReference().equalsIgnoreCase(taskForce.getReference());
    }

    /**
     * Get the game grid for this task force grid.
     *
     * @return The task force grid's game grid.
     */
    public GameGrid getGameGrid() {
        TaskForce taskForce = taskForces.get(0);
        return gameMapProvider
                .get()
                .getGrid(taskForce.getReference())
                .orElse(null);
    }

    /**
     * Indicates if the task force is at a base.
     * @return True if the task force is at a base grid. False otherwise.
     */
    public boolean isBaseGrid() {
        TaskForce taskForce = taskForces.get(0);
        return taskForce.atFriendlyBase() || taskForce.atEnemyBase();
    }

    /**
     * Get the marker grid's patrol groups.
     *
     * @param airbaseGroup The airbase group whose patrol groups are retrieved.
     * @return The marker grids patrol groups.
     */
    @Override
    public Optional<PatrolGroups> getPatrolGroups(final AirbaseGroup airbaseGroup) {
        return Optional.of(airbaseGroup.getPatrolGroups());
    }

    /**
     * Get the marker grid's air missions.
     *
     * @return A map of the marker grid's air missions keyed by the mission's target.
     */
    @Override
    public Optional<Map<Target, List<AirMission>>> getMissions(final AirbaseGroup airbaseGroup) {
        return Optional.of(airbaseGroup
                .getMissions()
                .stream()
                .collect(Collectors.groupingBy(AirMission::getTarget)));
    }
}
