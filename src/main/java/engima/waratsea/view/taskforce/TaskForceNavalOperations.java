package engima.waratsea.view.taskforce;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class TaskForceNavalOperations {

    @Getter
    private final ChoiceBox<AirMissionType> missionType = new ChoiceBox<>();

    /**
     * Create the operation tab.
     *
     * @return A tab for the given operation.
     */
    public Tab createOperationTab() {
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
        return new VBox(missionLabel, missionType);
    }
}
