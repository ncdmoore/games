package engima.waratsea.view.airfield.mission;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import engima.waratsea.viewmodel.AirMissionViewModel;
import engima.waratsea.viewmodel.NationAirbaseViewModel;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class MissionView {
    @Getter private final TableView<AirMissionViewModel> table = new TableView<>();
    @Getter private final Button add = new Button("Add");
    @Getter private final Button edit = new Button("Edit");
    @Getter private final Button delete = new Button("delete");

    private final TitledPane titledPane = new TitledPane();

    /**
     * Show the mission details view.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled pane that contains the mission view.
     */
    public MissionView build(final Nation nation) {
        titledPane.setText("Missions");

        setupTable();
        Node buttons = buildButtons();

        VBox vBox = new VBox(table, buttons);

        titledPane.setContent(vBox);

        return this;
    }

    /**
     * Bind this view to the given view model.
     *
     * @param viewModel The airfield view model.
     * @return This airfield ready view.
     */
    public TitledPane bind(final NationAirbaseViewModel viewModel) {
        table.itemsProperty().bind(viewModel.getMissions());
        return titledPane;
    }

    /**
     * Delete a mission from the mission table.
     *
     * @param mission The mission to delete.
     */
    public void deleteMissionFromTable(final AirMissionViewModel mission) {
        if (mission != null) {
            table.getItems().remove(mission);
            table.refresh();
        }
    }

    /**
     * Setup the mission table.
     */
    private void setupTable() {
        TableColumn<AirMissionViewModel, AirMissionType> typeColumn = new TableColumn<>("Mission Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().getMissionType());

        TableColumn<AirMissionViewModel, Target> targetColumn = new TableColumn<>("Target");
        targetColumn.setCellValueFactory(cellData -> cellData.getValue().getTarget());

        TableColumn<AirMissionViewModel, Integer> numSquadronColumn = new TableColumn<>("Squadrons");
        numSquadronColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalAssignedCount().asObject());

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
