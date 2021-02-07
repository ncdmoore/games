package engima.waratsea.view.taskforce;

import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

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

        ChoiceBox<AirbaseViewModel> airbases = new ChoiceBox<>();

        airbases.itemsProperty().bind(taskForceVM.getAirbases());
        airbases.getSelectionModel().selectFirst();

        Label label = new Label("Ships with aircraft:");
        VBox vBox = new VBox(label, airbases);

        tab.setContent(vBox);

        return tab;
    }


}
