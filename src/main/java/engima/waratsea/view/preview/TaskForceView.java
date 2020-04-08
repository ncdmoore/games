package engima.waratsea.view.preview;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.PreviewMapView;
import engima.waratsea.view.map.marker.preview.TargetMarker;
import engima.waratsea.view.ship.ShipViewType;
import engima.waratsea.view.taskforce.TaskForceViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the task forces summary view.
 */
public class TaskForceView {
    private static final String CSS_FILE = "taskForceView.css";

    private final ViewProps props;
    private final CssResourceProvider cssResourceProvider;
    private final ImageResourceProvider imageResourceProvider;

    @Getter private final ChoiceBox<TaskForce> taskForces = new ChoiceBox<>();
    @Getter private final Button continueButton = new Button("Continue");
    @Getter private final Button backButton = new Button("Back");

    @Getter private List<Button> shipButtons;

    private final Game game;
    private final PreviewMapView taskForceMap;

    private final Label stateValue = new Label();
    private final Text missionValue = new Text();
    private final Label reasonsValue = new Label();
    private final Label locationValue = new Label();

    private final Tab taskForceSummaryTab = new Tab("Summary");
    private final TabPane taskForceTabPane = new TabPane();
    private Map<ShipViewType, Tab> taskForceTabs;

    private final TableView<Pair<String, String>> shipTable = new TableView<>();
    private final TableView<Pair<String, String>> squadronTable = new TableView<>();

    private final ObjectProperty<Map<ShipViewType, List<Ship>>> shipTypeMap = new SimpleObjectProperty<>(this, "shipTypeMap", Collections.emptyMap());
    private final ObjectProperty<Map<AircraftType, BigDecimal>> squadronTypeMap = new SimpleObjectProperty<>(this, "squadronTypeMap", Collections.emptyMap());

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param game The game.
     * @param taskForceMap The task force preview map.
     */
    @Inject
    public TaskForceView(final ViewProps props,
                         final CssResourceProvider cssResourceProvider,
                         final ImageResourceProvider imageResourceProvider,
                         final Game game,
                         final PreviewMapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.taskForceMap = taskForceMap;
    }

    /**
     * Show the task forces summary view.
     *
     * @param stage The stage on which the task force scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Label title = new Label("Task Forces: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node objectivesPane = buildObjectives(scenario);

        Label labelPane = new Label("Task Forces:");
        labelPane.setId("label-pane");

        Node taskForceList = buildTaskForceList();
        Node pushButtons = buildPushButtons();

        Node map = taskForceMap.draw();

        HBox mapPane = new HBox(taskForceList, map);
        mapPane.setId("map-pane");

        Node taskForceDetails = buildTaskForceDetails();

        VBox vBox = new VBox(titlePane, objectivesPane, labelPane, mapPane, taskForceDetails, pushButtons);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Bind the view to he view model.
     *
     * @param viewModel The task force view model.
     */
    public void bind(final TaskForceViewModel viewModel) {
        stateValue.textProperty().bind(viewModel.getState());
        stateValue.textFillProperty().bind(viewModel.getStateColor());
        missionValue.textProperty().bind(viewModel.getMission());
        reasonsValue.textProperty().bind(viewModel.getReason());
        locationValue.textProperty().bind(viewModel.getLocation());

        shipTable.itemsProperty().bind(viewModel.getShipTypeSummary());
        bindTableHeight(shipTable, viewModel.getNumShipTypes());

        squadronTable.itemsProperty().bind(viewModel.getSquadronTypeSummary());
        bindTableHeight(squadronTable, viewModel.getNumSquadronTypes());

        shipTypeMap.bind(viewModel.getShipTypeMap());
        squadronTypeMap.bind(viewModel.getSquadronTypeMap());
    }

    /**
     * Bind the table height so that is shows just the rows present and no blank rows.
     *
     * @param table The table.
     * @param numRows The number of rows the table should have.
     */
    private void bindTableHeight(final TableView<Pair<String, String>> table, final IntegerProperty numRows) {
        int headerSize = props.getInt("taskForce.summary.table.cell.header.size");

        table.prefHeightProperty()
                .bind(Bindings.createDoubleBinding(() -> numRows.getValue() * table.getFixedCellSize() + headerSize, numRows));

        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());
    }

    /**
     * Build the selected scenario objective's text.
     *
     * @param scenario The selected scenario.
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final Scenario scenario) {
        Label objectiveLabel = new Label("Objectives:");
        Label objectiveValue = new Label(scenario.getObjectives());
        ImageView flag = imageResourceProvider.getImageView(scenario.getName(), props.getString(game.getHumanSide().toLower() + ".flag.medium.image"));

        HBox hBox = new HBox(flag, objectiveLabel, objectiveValue);
        hBox.setId("objective-pane");

        return hBox;
    }

    /**
     * build the task force list.
     *
     * @return Node containing the task force list.
     */
    private Node buildTaskForceList() {
        taskForces.setMaxWidth(props.getInt("taskForce.list.width"));
        taskForces.setMinWidth(props.getInt("taskForce.list.width"));

        VBox vBox = new VBox(taskForces, buildTaskForceStateDetails(), buildLegend());
        vBox.setId("taskforce-vbox");

        return vBox;
    }

    /**
     * Build the detailed information about the task force.
     *
     * @return A node that contains the task force details.
     */
    private Node buildTaskForceDetails() {
        HBox hBox = new HBox(buildShipDetails());
        hBox.setId("details-pane");
        return hBox;
    }

    /**
     * Build the task force state and mission details.
     *
     * @return A node containing the task force state and mission details.
     */
    private Node buildTaskForceStateDetails() {
        Text stateLabel = new Text("State:");
        Text missionLabel = new Text("Mission:");
        Text locationLabel = new Text("Location:");
        reasonsValue.setWrapText(true);

        GridPane gridPane = new GridPane();
        gridPane.setId("taskforce-details-grid");
        gridPane.add(missionLabel, 0, 0);
        gridPane.add(missionValue, 1, 0);
        gridPane.add(stateLabel, 0, 1);
        gridPane.add(stateValue, 1, 1);
        gridPane.add(locationLabel, 0, 2);
        gridPane.add(locationValue, 1, 2);

        VBox vBox = new VBox(gridPane, reasonsValue);
        vBox.setId("taskforce-details-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Task Force Details");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));
        titledPane.setId("taskforce-details-pane");

        return titledPane;
    }

    /**
     * Build the task force preview map legend.
     *
     * @return The node that contains the task force preview map legend.
     */
    private Node buildLegend() {
        VBox vBox = new VBox(taskForceMap.getLegend());
        vBox.setId("map-legend-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Map Legend");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));
        titledPane.setId("map-legend-pane");

        return titledPane;
    }

    /**
     * Build the ship details. This the ships within a task force.
     *
     * @return A node that contains the ship detailed information of the task force.
     */
    private Node buildShipDetails() {
        buildTable(shipTable, "Ships", "Number");
        buildTable(squadronTable, "Squadrons", "Steps");

        taskForceTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        taskForceTabPane.setMinWidth(props.getInt("taskForce.tabPane.width"));
        taskForceTabPane.setMaxWidth(props.getInt("taskForce.tabPane.width"));
        taskForceTabPane.setMinHeight(props.getInt("taskForce.tabPane.height"));

        taskForceTabPane.getTabs().add(taskForceSummaryTab);

        List<ShipViewType> shipViewTypes = Arrays.asList(ShipViewType.values());

        taskForceTabs = shipViewTypes.stream()
                .collect(Collectors.toMap(type -> type, this::buildTab));

        taskForceTabPane.getTabs().addAll(taskForceTabs.keySet()
                .stream()
                .sorted()
                .map(taskForceTabs::get)
                .collect(Collectors.toList()));

        return taskForceTabPane;
    }

    /**
     * Build a task force details ship tab.
     * @param type The ship view type.
     * @return The ship tab.
     */
    private Tab buildTab(final ShipViewType type) {
        return new Tab(type.toString());
    }

    /**
     * build the task force push buttons.
     *
     * @return Node containing the push buttons.
     */
    private Node buildPushButtons() {
        HBox hBox =  new HBox(backButton, continueButton);
        hBox.setId("push-buttons");
        return hBox;
    }

    /**
     * Set the task forces.
     *
     * @param forces The task forces.
     */
    public void setTaskForces(final List<TaskForce> forces) {
        taskForces.getItems().clear();
        taskForces.getItems().addAll(forces);
    }

    /**
     * Set the selected task force. Show this task force's map marker.
     *
     * @param taskForce the selected task force.
     */
    public void setSelectedTaskForce(final TaskForce taskForce) {
        taskForceMap.selectMarker(taskForce.getName());
        setTabs();
    }

    /**
     * Clear the selected task force marker.
     *
     * @param taskForce the task force whose marker is cleared.
     */
    public void clearTaskForce(final TaskForce taskForce) {
        taskForceMap.clearMarker(taskForce.getName());
    }

    /**
     * Place a task force marker on the preview map.
     *
     * @param dto Task force data transfer object.
     */
    public void markTaskForceOnMap(final AssetMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markTaskForce(dto);
    }

    /**
     * Place a target marker on the preview map.
     *
     * @param dto Target data transfer object.
     * @return The target marker.
     */
    public TargetMarker markTargetOnMap(final TargetMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        return taskForceMap.markTarget(dto);
    }

    /**
     * Finish the task force preview map.
     */
    public void finish() {
        taskForceMap.finish();
    }

    /**
     * Select a target. Show the corresponding popup.
     *
     * @param clickedMarker The target's marker.
     */
    public void selectTarget(final Object clickedMarker) {
        taskForceMap.selectTargetMarker(clickedMarker);
    }

    /**
     * Close the popup.
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
       taskForceMap.closePopup(event);
    }

    /**
     * Determine which task force details tabs are active for the selected task force.
     **/
    private void setTabs() {
        shipButtons = new ArrayList<>();

        setSummaryTab();

        taskForceTabs.values().forEach(tab -> tab.setDisable(true));

        shipTypeMap.getValue().forEach(this::setTabContents);

        // We clear and re-add the tabs to get around a javafx bug with refreshing the tab.
        // If we don't do this javafx only refreshes the tab contents when the mouse moves
        // over the tab area. This is a randomly occurring problem.
        taskForceTabPane.getTabs().clear();
        taskForceTabPane.getTabs().add(taskForceSummaryTab);
        taskForceTabPane.getTabs().addAll(taskForceTabs.keySet()
                .stream()
                .sorted()
                .map(taskForceTabs::get)
                .collect(Collectors.toList()));
    }

    /**
     * Set the information in the task force summary tab.
     **/
    private void setSummaryTab() {
        HBox hBox = new HBox(new VBox(shipTable));
        hBox.setId("taskforce-summary-hbox");

        if (!squadronTypeMap.getValue().isEmpty()) {             // Only show squadron summary if squadrons are present.
            hBox.getChildren().add(new VBox(squadronTable));
        }

        taskForceSummaryTab.setContent(hBox);
    }

    /**
     * Build a table of summary data.
     *
     * @param table The table.
     * @param mainLabel The main column's text.
     * @param numberLabel The number column's text.
     */
    private void buildTable(final TableView<Pair<String, String>> table, final String mainLabel, final String numberLabel) {
        TableColumn<Pair<String, String>, String> typeColumn = new TableColumn<>(mainLabel);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<Pair<String, String>, String> numberColumn = new TableColumn<>(numberLabel);
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().add(typeColumn);
        table.getColumns().add(numberColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);   // Needed to keep extra space from being seen after the last column.
        table.setFixedCellSize(props.getInt("taskForce.summary.table.cell.size"));
    }

    /**
     * Set a task force tab contents.
     *
     * @param viewType The type of ship that is contained within the tab.
     * @param ships A list of ships of the given type.
     */
    private void setTabContents(final ShipViewType viewType, final List<Ship> ships) {

        if (ships.isEmpty()) {
            return;
        }

        Tab tab = taskForceTabs.get(viewType);
        tab.setDisable(false);

        TilePane tilePane = new TilePane();
        tilePane.setId("taskforce-pane");
        tab.setContent(tilePane);

        ships.forEach(ship -> {
            Button button = new Button(ship.getTitle());
            button.setUserData(ship);
            button.setMinWidth(props.getInt("taskForce.ship.label.width"));
            button.setMaxWidth(props.getInt("taskForce.ship.label.width"));
            tilePane.getChildren().add(button);
            shipButtons.add(button);
        });

        ScrollPane sp = new ScrollPane();
        sp.setContent(tilePane);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        tab.setContent(sp);
    }
}
