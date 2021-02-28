package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import javafx.scene.Node;

/**
 * Represents an individual task force naval view.
 */
public class TaskForceNavalView {
    private final TaskForceNavalOperations taskForceNavalOperations;

    /**
     * The constructor called by guice.
     *
     * @param taskForceNavalOperations Task Force Naval operations view.
     */
    @Inject
    public TaskForceNavalView(final TaskForceNavalOperations taskForceNavalOperations) {
        this.taskForceNavalOperations = taskForceNavalOperations;
    }

    /**
     * Build the task force node.
     *
     * @param taskForceViewModel The task force view model.
     * @return The task force's view node.
     */
    public Node build(final TaskForceViewModel taskForceViewModel) {
        TaskForceNavalViewModel taskForceNavalViewModel = taskForceViewModel.getTaskForceNavalViewModel();
        return taskForceNavalOperations.build(taskForceNavalViewModel);
    }
}
