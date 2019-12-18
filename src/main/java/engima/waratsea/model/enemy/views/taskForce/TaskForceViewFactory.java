package engima.waratsea.model.enemy.views.taskForce;

import engima.waratsea.model.enemy.views.taskForce.data.TaskForceViewData;

/**
 * Factory used by guice to create task force views.
 */
public interface TaskForceViewFactory {
    /**
     * Creates a task force view.
     * @param data task force view data read from a JSON file.
     * @return A task force view initialized with the data from the JSON file.
     */
    TaskForceView create(TaskForceViewData data);
}
