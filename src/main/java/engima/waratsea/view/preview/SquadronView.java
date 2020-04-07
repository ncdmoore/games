package engima.waratsea.view.preview;

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
import engima.waratsea.presenter.dto.map.AssetMarkerDTO;
import engima.waratsea.presenter.squadron.Deployment;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.PreviewMapView;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
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
    private static final String ROUNDEL = ".roundel.image";

    private ViewProps props;
    private CssResourceProvider cssResourceProvider;
    private ImageResourceProvider imageResourceProvider;

    @Getter private TabPane nationsTabPane;
    @Getter private Map<Nation, ChoiceBox<Region>> regions = new HashMap<>();
    @Getter private Map<Nation, ChoiceBox<Airfield>> airfields = new HashMap<>();
    @Getter private Map<Nation, TableView<Deployment>> deploymentStats = new HashMap<>();
    @Getter private ListView<Squadron> availableSquadrons = new ListView<>();
    @Getter private Label squadronAirfieldLabel = new Label();
    @Getter private ListView<Squadron> airfieldSquadrons = new ListView<>();
    @Getter private Button deployButton = new Button();
    @Getter private Button removeButton = new Button();
    @Getter private Button detailsButton = new Button("Details");
    @Getter private Button continueButton = new Button("Continue");
    @Getter private Button backButton = new Button("Back");

    private Game game;
    private GameMap gameMap;

    private PreviewMapView taskForceMap;

    private Map<Nation, Label> regionMinimumValue = new HashMap<>();
    private Map<Nation, Label> regionMaximumValue = new HashMap<>();
    private Map<Nation, Label> airfieldMaximumValue = new HashMap<>();
    private Map<Nation, Map<String, ImageView>> regionDeployedValue = new HashMap<>();

    @Getter
    private Map<Nation, Label> airfieldCurrentValue = new HashMap<>();

    @Getter
    private Map<Nation, Map<AircraftBaseType, Label>> airfieldSteps = new HashMap<>();

    private Map<Boolean, Image> imageMap = new HashMap<>();

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
                        final PreviewMapView taskForceMap) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.gameMap = gameMap;
        this.taskForceMap = taskForceMap;

        Image redX = imageResourceProvider.getImage(props.getString("redX.image"));
        Image greenCheck = imageResourceProvider.getImage(props.getString("greenCheck.image"));

        imageMap.put(true, greenCheck);
        imageMap.put(false, redX);
    }

    /**
     * Bind the deployment stats table to the deployment stats.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @param deployment A list of deployment stats per aircraft landing type.
     */
    public void bindDeploymentStats(final Nation nation, final List<Deployment> deployment) {
        deploymentStats.put(nation, new TableView<>());
        deploymentStats.get(nation).setItems(FXCollections.observableArrayList(deployment));
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


        Node squadronsPane = buildSquadronsPane();

        VBox rightVBox = new VBox(map, squadronsPane);

        HBox mainPain = new HBox(nationTabPane, rightVBox);
        mainPain.setId("map-pane");

        VBox vBox = new VBox(titlePane, objectivesPane, mainPain, pushButtons);

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
    public void markAirfieldOnMap(final AssetMarkerDTO dto) {
        dto.setXOffset(props.getInt("taskforce.previewMap.popup.xOffset"));
        taskForceMap.markAirfield(dto);
    }

    /**
     * Mark a squadron's range radius on the preview map.
     *
     * @param dto The data transfer object.
     */
    public void markSquadronRangeOnMap(final AssetMarkerDTO dto) {
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
        regionMaximumValue.get(nation).setText(region.getMaxSteps() + "");
        regionMinimumValue.get(nation).setText(region.getMinSteps() + "");
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
     * Update the region's currently deployed steps.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    public void updateRegion(final Nation nation) {
        gameMap.getNationRegions(game.getHumanSide(), nation).forEach(region -> {
            if (regionDeployedValue.get(nation).containsKey(region.getName())) {
                ImageView imageView = regionDeployedValue.get(nation).get(region.getName());
                imageView.setImage(imageMap.get(region.minimumSatisfied()));
            }
        });
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
                .map(nation -> nation.toString() + ".flag.image")
                .map(flagName -> props.getString(flagName))
                .map(flagImage -> imageResourceProvider.getImageView(scenario.getName(), flagImage))
                .collect(Collectors.toList());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(nationFlags);
        hBox.getChildren().addAll(objectiveLabel, objectiveValue);
        hBox.setId("objective-pane");

        return hBox;
    }

    /**
     * Build the region details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @return A node containing the region details.
     */
    private Node buildRegionDetails(final Nation nation) {
        Text regionMinimumLabel = new Text("Region Minimum:");
        Text regionMaximumLabel = new Text("Region Maximum:");

        Label regionMaximum = new Label();
        regionMaximumValue.put(nation, regionMaximum);

        Label regionMinimum = new Label();
        regionMinimumValue.put(nation, regionMinimum);

        GridPane gridPane = new GridPane();
        gridPane.setId("region-details-grid");

        gridPane.add(regionMinimumLabel, 0, 0);
        gridPane.add(regionMinimum, 1, 0);
        gridPane.add(regionMaximumLabel, 0, 1);
        gridPane.add(regionMaximum, 1, 1);

        VBox vBox = new VBox(gridPane);
        vBox.setId("region-details-vbox");

        return vBox;
    }


    /**
     * Build the task force state and mission details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @return A node containing the airfield details.
     */
    private TitledPane buildAirfieldDetails(final Nation nation) {

        Text airfieldMaximumLabel = new Text("Airfield Maximum:");
        Text airfieldCurrentLabel = new Text("Airfield Current:");

        Label airfieldMaximum = new Label();
        airfieldMaximumValue.put(nation, airfieldMaximum);

        Label airfieldCurrent = new Label();
        airfieldCurrentValue.put(nation, airfieldCurrent);

        GridPane gridPane = new GridPane();
        gridPane.setId("airfield-details-grid");

        gridPane.add(airfieldMaximumLabel, 0, 0);
        gridPane.add(airfieldMaximum, 1, 0);
        gridPane.add(airfieldCurrentLabel, 0, 1);
        gridPane.add(airfieldCurrent, 1, 1);

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
        titledPane.setId("map-legend-pane");

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
                .sorted()
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

        Node regionDetails = buildRegionDetails(nation);

        VBox regionVBox = new VBox(regionLabel, regionChoiceBox, regionDetails);

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

        VBox vBox = new VBox(regionVBox, airfieldVBox, accordion, buildLegend(nation), buildDeployment(nation),  buildRegionDeployment(nation));
        vBox.setId("squadron-vbox");
        vBox.setMinHeight(props.getInt("squadron.left.vbox.length"));

        tab.setContent(vBox);

        ImageView roundel = imageResourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);
        return tab;
    }

    /**
     * Build the deployment summary deploymentStats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     * @return The deployment summary deploymentStats.
     */
    private Node buildDeployment(final Nation nation) {
        Label label = new Label("Squadron Step Summary:");

        TableColumn<Deployment, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Deployment, String> totalColumn = new TableColumn<>("Total\nSteps");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalSteps"));

        TableColumn<Deployment, String> deployedColumn = new TableColumn<>("Deployed\nSteps");
        deployedColumn.setCellValueFactory(new PropertyValueFactory<>("deployedSteps"));

        TableView<Deployment> stats = deploymentStats.get(nation);

        stats.getColumns().add(typeColumn);
        stats.getColumns().add(totalColumn);
        stats.getColumns().add(deployedColumn);
        stats.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);   // Needed to keep extra space from being seen after the last column.
        stats.setFixedCellSize(props.getInt("squadron.summary.table.cell.size"));
        stats.prefHeightProperty()
                .bind(Bindings.size(stats.getItems())
                        .multiply(stats.getFixedCellSize())
                        .add(props.getInt("squadron.summary.table.cell.header.size")));
        stats.minHeightProperty().bind(stats.prefHeightProperty());
        stats.maxHeightProperty().bind(stats.prefHeightProperty());

        stats.setMinWidth(props.getInt("squadron.summary.width"));
        stats.setMaxWidth(props.getInt("squadron.summary.width"));

        return new VBox(label, stats);
    }


    /**
     * Build the region deployment.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @return The node containing the region deployment.
     */
    private Node buildRegionDeployment(final Nation nation) {
        List<Region> regionsWithMinimum = gameMap
                .getNationRegions(game.getHumanSide(), nation)
                .stream()
                .filter(Region::hasMinimumRequirement)
                .collect(Collectors.toList());

        Map<String, ImageView> deployedMap = new HashMap<>();

        regionDeployedValue.put(nation, deployedMap);

        VBox vBox = new VBox();

        if (regionsWithMinimum.size() > 0) {

            GridPane gridPane = new GridPane();
            gridPane.setId("region-status-grid");

            int row = 0;
            for (Region region : regionsWithMinimum) {
                Label name = new Label(region.getName() + ":");
                Image image = imageMap.get(region.minimumSatisfied());
                ImageView deployed = new ImageView(image);
                name.setTooltip(new Tooltip("Indicates if region minimum squadron deployment satisfied"));

                gridPane.add(name, 0, row);
                gridPane.add(deployed, 1, row);

                deployedMap.put(region.getName(), deployed);

                row++;
            }

            Label label = new Label("Region Mininum Requirements:");
            VBox grid = new VBox(gridPane);
            vBox.getChildren().addAll(label, grid);
            grid.setId("region-status-vbox");
        }

        return vBox;
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

        deployButton.setGraphic(imageResourceProvider.getImageView(props.getString("right.arrow.image")));
        removeButton.setGraphic(imageResourceProvider.getImageView(props.getString("left.arrow.image")));

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
