package engima.waratsea.model.base;

import engima.waratsea.model.taskForce.patrol.PatrolGroups;

import java.util.List;

/**
 * Represents a group of airbases that share the same location. This only happens with task forces that contain
 * multiple ships (which implement the airbase interface).
 */
public interface AirbaseGroup {
    /**
     * Get the airbases in this group.
     *
     * For airfields there is just a single airbase in the group: the airfield.
     * For task forces there are multiple airbases: one for each aircraft carrier and capital ship within the task force.
     *
     * @return All the airbases within this air base group.
     */
    List<Airbase> getAirbases();

    /**
     * Get the airbase group's patrol groups.
     *
     * @return The airbase group's patrol groups.
     */
    PatrolGroups getPatrolGroups();
}
