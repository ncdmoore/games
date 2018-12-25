package engima.waratsea.model.ships;

/**
 * Factory used by guice to create task forces.
 */
public interface TaskForceFactory {
    /**
     * Creates a Task Force.
     * @param data Task force data read from a JSON file.
     * @return A Task Force initialized with the data from the JSON file.
     */
    TaskForce create(TaskForceData data);
}
