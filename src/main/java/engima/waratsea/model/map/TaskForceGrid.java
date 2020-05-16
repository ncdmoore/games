package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a task force grid on the game map.
 * Note, that a task force grid moves as the task force moves across the map.
 *
 * As long as the task force exists this task force grid will exist.
 *
 * If a two task forces are combined then one of the task force grids is removed.
 */
public class TaskForceGrid implements MarkerGrid {

    private final Provider<GameMap> gameMapProvider;

    @Getter private Side side;
    @Getter private List<TaskForce> taskForces = new ArrayList<>();

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
        return this;
    }

    /**
     * Add a task force to this grid.
     *
     * @param taskForce The task force to add.
     */
    public void add(final TaskForce taskForce) {
        taskForces.add(taskForce);
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
     * Get the base grid's map reference.
     *
     * @return This base grid's map reference.
     */
    public String getReference() {
        return getGameGrid().getMapReference();
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
     * Get the marker grid's patrol radii map.
     *
     * @return A map of the true maximum patrol radius to a list of
     * patrols that can reach that true maximum radius.
     */
    @Override
    public Optional<Map<Integer, List<Patrol>>> getPatrolRadiiMap() {

        // Will need to get all the task force's patrols and the build a combined map.


        return Optional.empty();
    }
}
