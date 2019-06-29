package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.TaskForcePreviewMapView;
import engima.waratsea.view.ships.ShipViewType;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the task forces summary view.
 */
public class TaskForceView {
    private static final String CSS_FILE = "taskForceView.css";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    @Getter
    private ChoiceBox<TaskForce> taskForces = new ChoiceBox<>();

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    @Getter
    private List<Button> shipButtons;

    private Game game;
    private TaskForcePreviewMapView taskForceMap;

    private Label stateValue = new Label();
    private Text missionValue = new Text();
    private Label reasonsValue = new Label();
    private Label locationValue = new Label();

    private Tab taskForceSummaryTab = new Tab("Summary");
    private TabPane taskForceTabPane = new TabPane();
    private Map<ShipViewType, Tab> taskForceTabs = new HashMap<>();

    private Map<Side, String> flags = new HashMap<>();

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
                         final TaskForcePreviewMapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.taskForceMap = taskForceMap;

        flags.put(Side.ALLIES, "alliesFlag50x34.png");
        flags.put(Side.AXIS, "axisFlag50x34.png");
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
     * Build the selected scenario objective's text.
     *
     * @param scenario The selected scenario.
     * @return The node that contains the selected scenario objective information.
     */
    private Node buildObjectives(final Scenario scenario) {
        Label objectiveLabel = new Label("Objectives:");
        Label objectiveValue = new Label(scenario.getObjectives());
        ImageView flag = imageResourceProvider.getImageView(scenario.getName(), flags.get(game.getHumanSide()));

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
     * @return A node containing the task foce state and mission details.
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

        return titledPane;
    }


    /**
     * Build the ship details. This the ships within a task force.
     *
     * @return A node that contains the ship detailed information of the task force.
     */
    private Node buildShipDetails() {
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
        String name = taskForce.getName();
        taskForceMap.selectMarker(name);

        stateValue.setText(taskForce.getState().toString());
        setStateColor(taskForce);
        missionValue.setText(taskForce.getMission().toString());

        String prefix = taskForce.atFriendlyBase() ? "At port " : "At sea zone ";

        locationValue.setText(prefix + taskForce.getMappedLocation());

        List<String> reasons = taskForce.getActivatedByText();

        reasonsValue.setText(String.join("\n", reasons));
        setTabs(taskForce);
    }

    /**
     * Clear the selected task force marker.
     *
     * @param taskForce the task force whose marker is cleared.
     */
    public void clearTaskForce(final TaskForce taskForce) {
        String name = taskForce.getName();
        taskForceMap.clearMarker(name);
    }

    /**
     * Place a task force marker on the preview map.
     *
     * @param dto Task force data transfer object.
     */
    public void markTaskForceOnMap(final TaskForceMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markTaskForce(dto);
    }

    /**
     * Place a target marker on the preview map.
     *
     * @param dto Target data transfer object.
     */
    public void markTargetOnMap(final TargetMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markTarget(dto);
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
     * Set the color of the task force state's text field.
     * @param taskForce The selected task force.
     */
    private void setStateColor(final TaskForce taskForce) {
        Paint color = taskForce.getState() == TaskForceState.RESERVE ? Color.RED : Color.BLACK;
        stateValue.setTextFill(color);
    }

    /**
     * Determine which task force details tabs are active for the selected task force.
     *
     * @param taskForce The selected task force.
     */
    private void setTabs(final TaskForce taskForce) {

        Map<ShipViewType, List<Ship>> shipViewTypeMap = getShipViewTypeMap(taskForce);
        Map<AircraftType, BigDecimal> squadronTypeMap = getAircraftTypeMap(taskForce);

        shipButtons = new ArrayList<>();

        setSummaryTab(shipViewTypeMap, squadronTypeMap);
        taskForceTabs.values().forEach(tab -> tab.setDisable(true));
        shipViewTypeMap.forEach(this::setTabContents);
        taskForceTabPane.getSelectionModel().selectLast();   // Don't remove this. This is to work around some javafx bug. If this is not here then the summary tab is not drawn correct.
        taskForceTabPane.getSelectionModel().selectFirst();
    }

    /**
     * Set the information in the task force summary tab.
     *
     * @param shipViewTypeMap The selected task force map of view type's to ships.
     * @param squadronTypeMap The selected task force map of aircraft type to number of steps.
     */
    private void setSummaryTab(final Map<ShipViewType, List<Ship>> shipViewTypeMap,
                               final Map<AircraftType, BigDecimal> squadronTypeMap) {
        List<Pair<String, String>> shipRows = shipViewTypeMap
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> new Pair<>(entry.getKey().toString(), entry.getValue().size() + ""))
                .collect(Collectors.toList());
        ObservableList<Pair<String, String>> shipData = FXCollections.observableArrayList(shipRows);

        List<Pair<String, String>> squadronRows = squadronTypeMap
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getValue().compareTo(BigDecimal.ZERO) == 0))
                .map(entry -> new Pair<>(entry.getKey().toString(), stripFraction(entry.getValue().setScale(2, RoundingMode.HALF_UP) + "")))
                .collect(Collectors.toList());
        ObservableList<Pair<String, String>> squadronData = FXCollections.observableArrayList(squadronRows);

        HBox hBox = new HBox(buildTable("Ships", "Number", shipData));
        hBox.setId("taskforce-summary-hbox");

        if (!squadronTypeMap.isEmpty()) {
            hBox.getChildren().add(buildTable("Squadrons", "Steps", squadronData));
        }

        taskForceSummaryTab.setContent(hBox);
    }

    /**
     * Map the ship type data into ship view type data. There are many types of ships. But to keep the GUI simple
     * there is a limited number of ship view types. This method maps all the game ship types into ship view types.
     *
     * @param taskForce The selected task force.
     * @return A map keyed by ship view type that contains the a list of ships.
     * Map[shipViewType] -> list of ships of that type.
     */
    private Map<ShipViewType, List<Ship>> getShipViewTypeMap(final TaskForce taskForce) {

        Map<ShipViewType, List<Ship>> shipViewTypeMap = new HashMap<>();

        Arrays.stream(ShipViewType.values())
                .forEach(viewType -> shipViewTypeMap.put(viewType, new ArrayList<>()));

        taskForce.getShipTypeMap()
                .forEach((type, ships) -> {
                    ShipViewType viewType = ShipViewType.get(type);
                    shipViewTypeMap.get(viewType).addAll(ships);
                });

        return shipViewTypeMap;
    }

    /**
     * Get a map of aircraft types within the given task force to the number of steps of the types.
     *
     * @param taskForce The selected task force.
     * @return A map of aircraft type to number of steps of the type.
     */
    private Map<AircraftType, BigDecimal> getAircraftTypeMap(final TaskForce taskForce) {
        return taskForce.getShips()
                .stream()
                .map(Ship::getSquadronSummary)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, BigDecimal::add));
    }

    /**
     * Build a table of summary data.
     *
     * @param mainLabel The main column's text.
     * @param numberLabel The number column's text.
     * @param data The summary data.
     * @return A node containing the summary table.
     */
    private Node buildTable(final String mainLabel, final String numberLabel, final ObservableList<Pair<String, String>> data) {
        TableView<Pair<String, String>> table = new TableView<>();

        TableColumn<Pair<String, String>, String> typeColumn = new TableColumn<>(mainLabel);
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<Pair<String, String>, String> numberColumn = new TableColumn<>(numberLabel);
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().add(typeColumn);
        table.getColumns().add(numberColumn);
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);   // Needed to keep extra space from being seen after the last column.
        table.setFixedCellSize(props.getInt("taskForce.summary.table.cell.size"));
        table.prefHeightProperty()
                .bind(Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(props.getInt("taskForce.summary.table.cell.header.size")));

        return new VBox(table);
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

    /**
     * Strip off the trailing zero's.
     *
     * @param number A number string.
     * @return If the given number has a zero fraction, then the number is returned with
     * no trailing zeros.
     */
    private String stripFraction(final String number) {
        String decimal = number.substring(number.indexOf('.') + 1);

        return (Integer.parseInt(decimal) == 0)
                ? number.substring(0, number.indexOf('.'))
                : number;
    }
}
