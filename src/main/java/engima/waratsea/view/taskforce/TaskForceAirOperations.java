package engima.waratsea.view.taskforce;

import engima.waratsea.viewmodel.TaskForceViewModel;
import javafx.scene.control.Tab;

public class TaskForceAirOperations {
    private TaskForceViewModel taskForceViewModel;

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Tab createOperationTab(final TaskForceViewModel taskForceVM) {
        taskForceViewModel = taskForceVM;

        Tab tab = new Tab();
        tab.setText("Air Operations");

        return tab;
    }
}
