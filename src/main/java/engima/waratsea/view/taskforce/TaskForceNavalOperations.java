package engima.waratsea.view.taskforce;

import com.google.inject.Inject;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.TaskForceViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class TaskForceNavalOperations {

    private TaskForceViewModel taskForceViewModel;

    @Getter private final ChoiceBox<SeaMissionType> missionType = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public TaskForceNavalOperations(final ViewProps props) {
        missionType.setMinWidth(props.getInt("mission.type.list.width"));
    }

    /**
     * Create the operation tab.
     *
     * @param taskForceVM The task force view model.
     * @return A tab for the given operation.
     */
    public Tab createOperationTab(final TaskForceViewModel taskForceVM) {
        taskForceViewModel = taskForceVM;

        Tab tab = new Tab();
        tab.setText("Naval Operations");

        Node missionNode = buildMissionNode();
        tab.setContent(missionNode);

        return tab;
    }

    /**
     * Build the mission node.
     *
     * @return A node containing the mission selection controls.
     */
    private Node buildMissionNode() {
        Label missionLabel = new Label("Select Mission Type:");

        missionType.getItems().addAll(taskForceViewModel.getMissionTypes().getValue());
        missionType.getSelectionModel().select(SeaMissionType.valueOf(taskForceViewModel.getMission().getValue().toUpperCase()));

        return new VBox(missionLabel, missionType);
    }
}
