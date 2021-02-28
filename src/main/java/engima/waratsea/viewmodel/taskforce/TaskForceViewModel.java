package engima.waratsea.viewmodel.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.viewmodel.taskforce.air.TaskForceAirViewModel;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import lombok.Getter;

public class TaskForceViewModel {
    @Getter private final TaskForceNavalViewModel taskForceNavalViewModel;
    @Getter private final TaskForceAirViewModel taskForceAirViewModel;

    @Inject
    public TaskForceViewModel(final TaskForceNavalViewModel taskForceNavalViewModel,
                              final TaskForceAirViewModel taskForceAirViewModel) {
        this.taskForceNavalViewModel = taskForceNavalViewModel;
        this.taskForceAirViewModel = taskForceAirViewModel;
    }

    /**
     * Set the backing task force model.
     *
     * @param taskForce The task force model.
     * @return This task force view model.
     */
    public TaskForceViewModel setModel(final TaskForce taskForce) {
        taskForceNavalViewModel.setModel(taskForce);
        taskForceAirViewModel.setModel(taskForce);
        return this;
    }

    /**
     * Save any changes in the view model to the model.
     */
    public void save() {
        taskForceAirViewModel.save();
    }
}
