package engima.waratsea.model.map;

import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.patrol.PatrolGroups;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a marker's grid on the game map. This may be a base grid or a task force grid.
 */
public interface MarkerGrid {

    /**
     * Get the marker grid's patrol groups.
     *
     * @return The marker grids patrol groups.
     */
    Optional<PatrolGroups> getPatrolGroups();

    /**
     * Get the marker grid's air missions.
     *
     * @return A map of the marker grid's air missions keyed by the mission's target.
     */
    Optional<Map<Target, List<AirMission>>> getMissions();
}
