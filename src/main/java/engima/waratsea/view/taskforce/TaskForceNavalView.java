package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * Represents an individual task force view.
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
     * Build the task force tab.
     *
     * @param taskForce The task force.
     * @return The task force's tab.
     */
    public Tab build(final TaskForceNavalViewModel taskForce) {
        Tab tab = new Tab();
        tab.textProperty().bind(taskForce.getNameAndTitle());

        Node content = taskForceNavalOperations.build(taskForce);

        tab.setContent(content);

        return tab;
    }
}
