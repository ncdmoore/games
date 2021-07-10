package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.viewmodel.airfield.RealAirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;

/**
 * Represents an individual task force air view.
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
     * @param taskForceViewModel The task force view model.
     * @return The task force's view node.
     */
    public Node build(final TaskForceViewModel taskForceViewModel) {
        TaskForceAirViewModel taskForceAirViewModel = taskForceViewModel.getTaskForceAirViewModel();
        return taskForceAirOperations.build(taskForceAirViewModel);
    }

    /**
     * Get the airbase choice box.
     *
     * @return The airbase choice box.
     */
    public ChoiceBox<RealAirbaseViewModel> getChoiceBox() {
        return taskForceAirOperations.getAirbases();
    }

    /**
     * Set the airbase.
     *
     * @param airbase The airbase view model that has been selected.
     */
    public void setAirbase(final RealAirbaseViewModel airbase) {
        taskForceAirOperations.airbaseSelected(airbase);
    }
}
