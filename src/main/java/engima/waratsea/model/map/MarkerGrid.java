package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.patrol.Patrol;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MarkerGrid {
    /**
     * Get the marker grid's patrol radii map.
     *
     * @return A map of the true maximum patrol radius to a list of
     * patrols that can reach that true maximum radius.
     */
    Optional<Map<Integer, List<Patrol>>> getPatrolRadiiMap();
}