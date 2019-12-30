package engima.waratsea.model.taskForce.mission;

import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.mission.data.MissionData;

import java.util.List;

public interface Mission {

    /**
     * Get the persistent mission data.
     *
     * @return The persistent mission data.
     */
    MissionData getData();

    /**
     * Get the mission type.
     *
     * @return The mission type.
     */
    MissionType getType();

    /**
     * Get a list of targets for this mission.
     *
     * @return A list of targets for this mission.
     */
    List<Target> getTargets();
}
