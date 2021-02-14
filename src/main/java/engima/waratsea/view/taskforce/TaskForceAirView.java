package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * Represents an individual task force view.
 */
public class TaskForceAirView {
    private final TaskForceAirOperations taskForceAirOperations;

    /**
     * The constructor called by guice.
     *
     * @param taskForceAirOperations Task Force Air operations view.
     */
    @Inject
    public TaskForceAirView(final TaskForceAirOperations taskForceAirOperations) {
        this.taskForceAirOperations = taskForceAirOperations;
    }

    /**
     * Build the task force tab.
     *
     * @param taskForce The task force.
     * @return The task force's tab.
     */
    public Tab build(final TaskForceAirViewModel taskForce) {
        Tab tab = new Tab();
        tab.textProperty().bind(taskForce.getNameAndTitle());

        Node contents = taskForceAirOperations.build(taskForce);

        tab.setContent(contents);

        return tab;
    }
}
