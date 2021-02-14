package engima.waratsea.viewmodel.taskforce.air;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.TaskForce;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.List;

/**
 * Represents the view model of a given side's task forces.
 */
public class TaskForcesAirViewModel {
    @Getter
    private final ListProperty<TaskForce> taskForces = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter
    private final TaskForceAirViewModel selectedTaskForce;                                                      // Selected task force.

    @Inject
    public TaskForcesAirViewModel(final TaskForceAirViewModel taskForceViewModel) {
        this.selectedTaskForce = taskForceViewModel;
    }

    /**
     * Set the task forces model.
     *
     * @param forces The task forces.
     */
    public void setModel(final List<TaskForce> forces) {
        taskForces.setValue(FXCollections.observableList(forces));
    }
}
