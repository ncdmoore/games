package engima.waratsea.view.airfield.mission;

import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;

/**
 * This class represents the mission view in the airfield dialog.
 *
 * CSS styles used.
 *
 *  - spacing-10
 */
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
        BooleanProperty noMissions = viewModel.getNoMissionsExist();

        ObservableList<AirMissionViewModel> selectedItems = table
                .getSelectionModel()
                .getSelectedItems();

        table.itemsProperty().bind(viewModel.getMissionViewModels());
        add.disableProperty().bind(viewModel.getNoSquadronsReady());
        edit.disableProperty().bind(Bindings.createBooleanBinding(
                () -> noMissions.getValue() || selectedItems.isEmpty(), noMissions, selectedItems));
        delete.disableProperty().bind(Bindings.createBooleanBinding(
                () -> noMissions.getValue() || selectedItems.isEmpty(), noMissions, selectedItems));

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
        TableColumn<AirMissionViewModel, Integer> idColumn = new TableColumn<>("Mission Id");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().getMissionId().asObject());

        TableColumn<AirMissionViewModel, AirMissionType> typeColumn = new TableColumn<>("Mission Type");
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().getMissionType());

        TableColumn<AirMissionViewModel, Target> targetColumn = new TableColumn<>("Target");
        targetColumn.setCellValueFactory(cellData -> cellData.getValue().getTarget());

        TableColumn<AirMissionViewModel, AirMissionState> stateColumn = new TableColumn<>("State");
        stateColumn.setCellValueFactory(cellData -> cellData.getValue().getState());

        TableColumn<AirMissionViewModel, Integer> numSquadronColumn = new TableColumn<>("Squadrons");
        numSquadronColumn.setCellValueFactory(cellData -> cellData.getValue().getTotalAssignedCount().asObject());

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);   //Limit the table columns to 3.
        table.getColumns().add(idColumn);
        table.getColumns().add(typeColumn);
        table.getColumns().add(targetColumn);
        table.getColumns().add(stateColumn);
        table.getColumns().add(numSquadronColumn);
    }

    /**
     * Build the mission control buttons.
     *
     * @return A node containing the mission control buttons
     */
    private Node buildButtons() {
        HBox hBox = new HBox(add, edit, delete);
        hBox.getStyleClass().add("spacing-10");
        return hBox;
    }
}
