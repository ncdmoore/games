package engima.waratsea.view.airfield.mission;

import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;


public class MissionView {

    @Getter
    private final TableView<Mission> table = new TableView<>();

    @Getter
    private final Button add = new Button("Add");

    @Getter
    private final Button edit = new Button("Edit");

    @Getter
    private final Button delete = new Button("delete");


    /**
     * Show the mission details view.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane that contains the mission view.
     */
    public TitledPane show(final Nation nation) {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Missions");

        setupTable();
        Node buttons = buildButtons();

        VBox vBox = new VBox(table, buttons);

        titledPane.setContent(vBox);

        return titledPane;
    }

    /**
     * Add a mission to the mission table.
     *
     * @param mission The mission to add.
     */
    public void addMissionToTable(final Mission mission) {
        table.getItems().add(mission);
        table.refresh();
    }

    /**
     * Delete a mission from the mission table.
     *
     * @param mission The mission to delete.
     */
    public void deleteMissionFromTable(final Mission mission) {
        table.getItems().remove(mission);
        table.refresh();
    }

    /**
     * Setup the mission table.
     */
    private void setupTable() {
        TableColumn<Mission, String> typeColumn = new TableColumn<>("Mission Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Mission, Target> targetColumn = new TableColumn<>("Target");
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));

        TableColumn<Mission, Integer> numSquadronColumn = new TableColumn<>("Squadrons");
        numSquadronColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);   //Limit the table columns to 3.
        table.getColumns().add(typeColumn);
        table.getColumns().add(targetColumn);
        table.getColumns().add(numSquadronColumn);
    }

    /**
     * Build the mission control buttons.
     *
     * @return A node containing the mission control buttons
     */
    private Node buildButtons() {
        HBox hBox = new HBox(add, edit, delete);
        hBox.setId("mission-control-pane");
        return hBox;
    }
}
