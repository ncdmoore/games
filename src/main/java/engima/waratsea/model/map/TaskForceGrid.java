package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.patrol.Patrol;

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
