package engima.waratsea.model.taskForce;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.taskForce.data.TaskForceData;

/**
 * Factory used by guice to create task forces.
 */
public interface TaskForceFactory {
    /**
     * Creates a Task Force.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Task force data read from a JSON file.
     * @return A Task Force initialized with the data from the JSON file.
     */
    TaskForce create(Side side, TaskForceData data);
}
