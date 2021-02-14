package engima.waratsea.viewmodel.taskforce.naval;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.TaskForce;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import lombok.Getter;

import java.util.List;

/**
 * Represents the view model of a given side's task forces.
 */
public class TaskForcesNavalViewModel {
    @Getter private final ListProperty<TaskForce> taskForces = new SimpleListProperty<>(FXCollections.emptyObservableList());

    @Getter private final TaskForceNavalViewModel selectedTaskForce;                                                         // Selected task force.
    @Getter private final BooleanProperty anyTaskForceNotSet = new SimpleBooleanProperty(true);               // Initially not all task forces are set.

    @Inject
    public TaskForcesNavalViewModel(final TaskForceNavalViewModel taskForceViewModel) {
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

    /**
     * Set the currently selected task force's location.
     *
     * @param newLocation The currently selected task force's new location.
     */
    public void setLocation(final String newLocation) {
        selectedTaskForce.setLocation(newLocation);

        // Determine if all of the task forces now have their locations set.
        anyTaskForceNotSet.setValue(anyTaskForceNotSet());
    }

    /**
     * Determine if all the task forces have their locations set.
     *
     * @return True if all of the task forces have their locations set. Otherwise, false is returned.
     */
    private boolean anyTaskForceNotSet() {
        return !taskForces
                .getValue()
                .stream()
                .allMatch(TaskForce::isLocationKnown);
    }
}
