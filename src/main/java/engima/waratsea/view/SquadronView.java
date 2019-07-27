package engima.waratsea.view;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.map.TaskForcePreviewMapView;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the squadron deployment view.
 */
public class SquadronView {
    private static final String CSS_FILE = "squadronView.css";
    private static final String ROUNDEL_SIZE = "20x20.png";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    @Getter
    private TabPane nationsTabPane;

    @Getter
    private Map<Nation, ChoiceBox<Region>> regions = new HashMap<>();

    @Getter
    private Map<Nation, ChoiceBox<Airfield>> airfields = new HashMap<>();

    @Getter
    private ListView<Squadron> availableSquadrons = new ListView<>();

    @Getter
    private Label squadronAirfieldLabel = new Label();

    @Getter
    private ListView<Squadron> airfieldSquadrons = new ListView<>();

    @Getter
    private Button deployButton = new Button();

    @Getter
    private Button removeButton = new Button();

    @Getter
    private Button detailsButton = new Button("Details");

    @Getter
    private Button continueButton = new Button("Continue");

    @Getter
    private Button backButton = new Button("Back");

    private Game game;
    private GameMap gameMap;

    private TaskForcePreviewMapView taskForceMap;

    private Map<Nation, String> flags = new HashMap<>();

    private Map<Nation, Label> regionMinimumValue = new HashMap<>();
    private Map<Nation, Label> regionMaximumValue = new HashMap<>();
    private Map<Nation, Label> airfieldMaximumValue = new HashMap<>();

    @Getter
    private Map<Nation, Label> airfieldCurrentValue = new HashMap<>();

    @Getter
    private Map<Nation, Map<AircraftBaseType, Label>> airfieldSteps = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param game The game.
     * @param gameMap The game map.
     * @param taskForceMap The task force preview map.
     */
    @Inject
    public SquadronView(final ViewProps props,
                        final CssResourceProvider cssResourceProvider,
                        final ImageResourceProvider imageResourceProvider,
                        final Game game,
                        final GameMap gameMap,
                        final TaskForcePreviewMapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.gameMap = gameMap;
        this.taskForceMap = taskForceMap;

        flags.put(Nation.BRITISH, "britishFlag50x34.png");
        flags.put(Nation.ITALIAN, "axisFlag50x34.png");
        flags.put(Nation.GERMAN, "germanFlag50x34.png");
        flags.put(Nation.FRENCH, "axisFlag50x34.png");
        flags.put(Nation.UNITED_STATES, "alliesFlag50x34.png");
        flags.put(Nation.JAPANESE, "axisFlag50x34.png");
        flags.put(Nation.AUSTRALIAN, "australian50x34.png");
    }

    /**
     * Show the task forces summary view.
     *
     * @param stage The stage on which the task force scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Label title = new Label("Squadrons: " + scenario.getTitle());
        title.setId("title");
        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        Node objectivesPane = buildObjectives(scenario);

        Node nationTabPane = buildNationTabs();
        Node pushButtons = buildPushButtons();

        Node map = taskForceMap.draw();

        HBox mapPane = new HBox(nationTabPane, map);
        mapPane.setId("map-pane");

        Node squadronsPane = buildSquadronsPane();

        VBox vBox = new VBox(titlePane, objectivesPane, mapPane, squadronsPane, pushButtons);

        int sceneWidth = props.getInt("taskForce.scene.width");
        int sceneHeight = props.getInt("taskForce.scene.height");

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight);

        scene.getStylesheets().add(cssResourceProvider.get(CSS_FILE));

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Place a task force marker on the preview map.
     *
     * @param dto Task force data transfer object.
     */
    public void markAirfieldOnMap(final TaskForceMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markAirfield(dto);
    }

    /**
     * Mark a squadron's range radius on the preview map.
     *
     * @param dto The data transfer object.
     */
    public void markSquadronRangeOnMap(final TaskForceMarkerDTO dto) {
        taskForceMap.markRange(dto);
    }

    /**
     * Clear a squadron's range radius on the preview map.
     */
    public void clearSquadronRange() {
        taskForceMap.clearRange();
    }

    /**
     * Set the selected regionl Show the region's details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @param region The selected region.
     */
    public void setSelectedRegion(final Nation nation, final Region region) {
        regionMaximumValue.get(nation).setText(region.getMax() + "");
        regionMinimumValue.get(nation).setText(region.getMin() + "");
    }

    /**
     * Set the selected airfield. Show this airfield's map marker and details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @param airfield The selected airfield.
     */
    public void setSelectedAirfield(final Nation nation, final Airfield airfield) {
        String name = airfield.getName();
        taskForceMap.selectAirfieldMarker(name);
        airfieldMaximumValue.get(nation).setText(airfield.getMaxCapacity() + "");
        airfieldCurrentValue.get(nation).setText(airfield.getCurrentSteps() + "");

        squadronAirfieldLabel.setText(airfield.getTitle() + " Squadrons:");

        airfieldSquadrons.getItems().clear();
        airfieldSquadrons.getItems().addAll(airfield.getSquadrons());

        GridPane gridPane = new GridPane();
        gridPane.setId("airfield-summary-grid");

        for (AircraftBaseType type : AircraftBaseType.values()) {
            airfieldSteps.get(nation).get(type).setText(airfield.getStepsForType(type) + "");
        }
    }

    /**
     * Clear an airfield marker.
     *
     * @param airfield the airfield whose marker is cleared.
     */
    public void clearAirfield(final Airfield airfield) {
        String name = airfield.getName();
        taskForceMap.clearAirfieldMarker(name);
    }

    /**
     * Remove an airfield marker.
     *
     * @param airfield the airfield whose marker is removed.
     */
    public void removeAirfield(final Airfield airfield) {
        String name = airfield.getName();
        taskForceMap.removeAirfieldMarker(name);
    }

    /**
     * Finish the task force preview map.
     */
    public void finish() {
        taskForceMap.finish();
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    public void closePopup(final MouseEvent event) {
        taskForceMap.closePopup(event);
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

        List<ImageView> nationFlags = game
                .getHumanPlayer()
                .getNations()
                .stream()
                .map(nation -> imageResourceProvider.getImageView(scenario.getName(), flags.get(nation)))
                .collect(Collectors.toList());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(nationFlags);
        hBox.getChildren().addAll(objectiveLabel, objectiveValue);
        hBox.setId("objective-pane");

        return hBox;
    }

    /**
     * Build the task force state and mission details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @return A node containing the task foce state and mission details.
     */
    private TitledPane buildAirfieldDetails(final Nation nation) {

        Text regionMinimumLabel = new Text("Region Minimum:");
        Text regionMaximumLabel = new Text("Region Maximum:");
        Text airfieldMaximumLabel = new Text("Airfield Maximum:");
        Text airfieldCurrentLabel = new Text("Airfield Current:");

        Label regionMaximum = new Label();
        regionMaximumValue.put(nation, regionMaximum);

        Label regionMinimum = new Label();
        regionMinimumValue.put(nation, regionMinimum);

        Label airfieldMaximum = new Label();
        airfieldMaximumValue.put(nation, airfieldMaximum);

        Label airfieldCurrent = new Label();
        airfieldCurrentValue.put(nation, airfieldCurrent);

        final int row3 = 3;
        GridPane gridPane = new GridPane();
        gridPane.setId("airfield-details-grid");
        gridPane.add(regionMinimumLabel, 0, 0);
        gridPane.add(regionMinimum, 1, 0);
        gridPane.add(regionMaximumLabel, 0, 1);
        gridPane.add(regionMaximum, 1, 1);
        gridPane.add(airfieldMaximumLabel, 0, 2);
        gridPane.add(airfieldMaximum, 1, 2);
        gridPane.add(airfieldCurrentLabel, 0, row3);
        gridPane.add(airfieldCurrent, 1, row3);

        VBox vBox = new VBox(gridPane);
        vBox.setId("airfield-details-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Airfield Details");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));
        titledPane.setId("airfield-details-pane");

        return titledPane;
    }

    /**
     * Build the squadron summary pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The titled pane that contains the squadron summary for the current airfield.
     */
    private TitledPane buildSquadronSummary(final Nation nation) {
        Map<AircraftBaseType, Label> airfieldStepMap = new HashMap<>();
        Stream.of(AircraftBaseType.values()).forEach(type -> airfieldStepMap.put(type, new Label()));
        airfieldSteps.put(nation, airfieldStepMap);

        GridPane gridPane = new GridPane();
        gridPane.setId("airfield-summary-grid");

        int row = 0;
        for (AircraftBaseType type : AircraftBaseType.values()) {
            gridPane.add(new Label(type.toString() + ":"), 0, row);
            gridPane.add(airfieldSteps.get(nation).get(type), 1, row);
            row++;
        }

        VBox vBox = new VBox(gridPane);
        vBox.setId("airfield-summary-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Squadron Summary");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));
        titledPane.setId("squadron-summary-pane");

        return titledPane;
    }

    /**
     * Build the airfield preview map legend.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     * @return The node that contains the airfield preview map legend.
     */
    private Node buildLegend(final Nation nation) {

        VBox vBox = new VBox(taskForceMap.getLegendAirfield(nation));
        vBox.setId("map-legend-vbox");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Map Legend");
        titledPane.setContent(vBox);

        titledPane.setMaxWidth(props.getInt("taskForce.details.width"));
        titledPane.setMinWidth(props.getInt("taskForce.details.width"));

        return titledPane;
    }

    /**
     * Build the nations tab pane.
     *
     * @return The nations tab pane.
     */
    private Node buildNationTabs() {
        nationsTabPane = new TabPane();
        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        nationsTabPane.setMinWidth(props.getInt("squadron.tabPane.width"));
        nationsTabPane.setMaxWidth(props.getInt("squadron.tabPane.width"));

        game
                .getHumanPlayer()
                .getNations()
                .stream()
                .map(this::buildTab)
                .forEach(tab -> nationsTabPane.getTabs().add(tab));

        return nationsTabPane;
    }

    /**
     * Build a nation tab.
     *
     * @param nation The nation.
     * @return The nation tab.
     */
    private Tab buildTab(final Nation nation) {
        Side side = game.getHumanSide();

        Tab tab = new Tab(nation.toString());

        Label regionLabel = new Label("Region:");
        ChoiceBox<Region> regionChoiceBox = new ChoiceBox<>();
        regionChoiceBox.getItems().addAll(gameMap.getNationRegions(side, nation));
        regionChoiceBox.setMinWidth(props.getInt("squadron.tabPane.width"));
        regionChoiceBox.setMaxWidth(props.getInt("squadron.tabPane.width"));
        VBox regionVBox = new VBox(regionLabel, regionChoiceBox);

        regions.put(nation, regionChoiceBox);

        Label airfieldLabel = new Label("Airfield:");
        ChoiceBox<Airfield> airfieldChoiceBox = new ChoiceBox<>();
        airfieldChoiceBox.getItems().addAll(gameMap.getNationAirfields(side, nation));
        airfieldChoiceBox.setMinWidth(props.getInt("squadron.tabPane.width"));
        airfieldChoiceBox.setMaxWidth(props.getInt("squadron.tabPane.width"));
        VBox airfieldVBox = new VBox(airfieldLabel, airfieldChoiceBox);

        airfields.put(nation, airfieldChoiceBox);

        Accordion accordion = new Accordion();
        TitledPane airfieldDetails = buildAirfieldDetails(nation);
        TitledPane squadronSummary = buildSquadronSummary(nation);
        accordion.getPanes().addAll(airfieldDetails, squadronSummary);
        accordion.setExpandedPane(airfieldDetails);

        VBox vBox = new VBox(regionVBox, airfieldVBox, accordion, buildLegend(nation));
        vBox.setId("squadron-vbox");

        tab.setContent(vBox);

        ImageView roundel = imageResourceProvider.getImageView(nation + ROUNDEL_SIZE);

        tab.setGraphic(roundel);
        return tab;
    }

    /**
     * Build the squadron pane.
     *
     * @return The squadron pane Hbox.
     */
    private Node buildSquadronsPane() {

        availableSquadrons.setMinHeight(props.getInt("squadron.list.width"));
        availableSquadrons.setMaxHeight(props.getInt("squadron.list.width"));
        availableSquadrons.setMinWidth(props.getInt("squadron.tabPane.width"));
        availableSquadrons.setMaxWidth(props.getInt("squadron.tabPane.width"));

        deployButton.setGraphic(imageResourceProvider.getImageView("rightArrow.png"));
        removeButton.setGraphic(imageResourceProvider.getImageView("leftArrow.png"));

        deployButton.setMinWidth(props.getInt("squadron.button.width"));
        deployButton.setMaxWidth(props.getInt("squadron.button.width"));
        removeButton.setMaxWidth(props.getInt("squadron.button.width"));
        removeButton.setMinWidth(props.getInt("squadron.button.width"));
        detailsButton.setMaxWidth(props.getInt("squadron.button.width"));
        detailsButton.setMinWidth(props.getInt("squadron.button.width"));


        VBox buttonVBox = new VBox(deployButton, removeButton, detailsButton);
        buttonVBox.setId("squadron-controls");

        airfieldSquadrons.setMinHeight(props.getInt("squadron.list.width"));
        airfieldSquadrons.setMaxHeight(props.getInt("squadron.list.width"));
        airfieldSquadrons.setMinWidth(props.getInt("squadron.tabPane.width"));
        airfieldSquadrons.setMaxWidth(props.getInt("squadron.tabPane.width"));

        Label availableLabel = new Label("Available Squadrons:");

        VBox availableVBox = new VBox(availableLabel, availableSquadrons);

        VBox airfieldVBox = new VBox(squadronAirfieldLabel, airfieldSquadrons);

        HBox hBox = new HBox(availableVBox, buttonVBox, airfieldVBox);
        hBox.setId("squadron-pane");

        return hBox;
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


}
