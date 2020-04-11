package engima.waratsea.view.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
import engima.waratsea.viewmodel.AirfieldViewModel;
import engima.waratsea.viewmodel.DeploymentViewModel;
import engima.waratsea.viewmodel.RegionViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the squadron deployment view.
 */
public class SquadronView {
    private static final String CSS_FILE = "squadronView.css";
    private static final String ROUNDEL = ".roundel.image";

    private final ViewProps props;
    private final CssResourceProvider cssResourceProvider;
    private final ImageResourceProvider imageResourceProvider;

    @Getter private TabPane nationsTabPane;
    @Getter private final Map<Nation, ChoiceBox<Region>> regions = new HashMap<>();
    @Getter private final Map<Nation, ChoiceBox<Airfield>> airfields = new HashMap<>();
    @Getter private final Map<Nation, TableView<Deployment>> deploymentStats = new HashMap<>();

    @Getter private final Map<Nation, ListView<Squadron>> availableSquadrons = new HashMap<>();
    @Getter private final Map<Nation, ListView<Squadron>> airfieldSquadrons = new HashMap<>();

    private final Map<Nation, Label> squadronAirfieldLabel = new HashMap<>();
    @Getter private final Map<Nation, Button> deployButtons = new HashMap<>();
    @Getter private final Map<Nation, Button> removeButtons = new HashMap<>();
    @Getter private final Map<Nation, Button> detailsButtons = new HashMap<>();

    @Getter private final Button continueButton = new Button("Continue");
    @Getter private final Button backButton = new Button("Back");

    private final Game game;
    private final GameMap gameMap;

    private final Map<Nation, PreviewMapView> previewMap = new HashMap<>();

    private final Map<Nation, Label> regionMinimumValue = new HashMap<>();
    private final Map<Nation, Label> regionMaximumValue = new HashMap<>();
    private final Map<Nation, Label> regionCurrentValue = new HashMap<>();
    private final Map<Nation, Label> airfieldMaximumValue = new HashMap<>();
    private final Map<Nation, Label> airfieldCurrentValue = new HashMap<>();
    private final Map<Nation, Label> airfieldAntiAirValue = new HashMap<>();

    private final Map<Nation, Map<Region, ImageView>> regionDeployedValue = new HashMap<>();

    @Getter private final Map<Nation, Map<AircraftBaseType, Label>> airfieldSteps = new HashMap<>();

    private final Map<Boolean, Image> imageMap;

    private final Set<Nation> nations;

    /**
     * Constructor called by guice.
     *
     * @param props view properties.
     * @param cssResourceProvider CSS file provider.
     * @param imageResourceProvider Image file provider.
     * @param mapProvider Provides the maps.
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public SquadronView(final ViewProps props,
                        final CssResourceProvider cssResourceProvider,
                        final ImageResourceProvider imageResourceProvider,
                        final Game game,
                        final GameMap gameMap,
                        final Provider<PreviewMapView> mapProvider) {

        this.props = props;
        this.cssResourceProvider = cssResourceProvider;
        this.imageResourceProvider = imageResourceProvider;
        this.game = game;
        this.gameMap = gameMap;

        Image redX = imageResourceProvider.getImage(props.getString("redX.image"));
        Image greenCheck = imageResourceProvider.getImage(props.getString("greenCheck.image"));

        imageMap = Map.of(true, greenCheck, false, redX);

        nations = game
                .getHumanPlayer()
                .getNations();

        nations
                .forEach(nation -> {
                    regionMinimumValue.put(nation, new Label());
                    regionMaximumValue.put(nation, new Label());
                    regionCurrentValue.put(nation, new Label());

                    airfieldMaximumValue.put(nation, new Label());
                    airfieldCurrentValue.put(nation, new Label());
                    airfieldAntiAirValue.put(nation, new Label());

                    regions.put(nation, new ChoiceBox<>());
                    airfields.put(nation, new ChoiceBox<>());

                    previewMap.put(nation, mapProvider.get());

                    deploymentStats.put(nation, new TableView<>());

                    availableSquadrons.put(nation, new ListView<>());
                    airfieldSquadrons.put(nation, new ListView<>());

                    deployButtons.put(nation, new Button());
                    removeButtons.put(nation, new Button());
                    detailsButtons.put(nation, new Button("Details"));

                    squadronAirfieldLabel.put(nation, new Label());

                    Map<AircraftBaseType, Label> airfieldStepMap = new HashMap<>();
                    Stream.of(AircraftBaseType.values()).forEach(type -> airfieldStepMap.put(type, new Label()));
                    airfieldSteps.put(nation, airfieldStepMap);

                    buildMinimumRegion(nation);
                });
    }

    /**
     * Bind this view to the region view model.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param viewModel The region view model.
     * @return This squadron view.
     */
    public SquadronView bind(final Nation nation, final RegionViewModel viewModel) {
        regionMinimumValue.get(nation).textProperty().bind(viewModel.getMinSteps());
        regionMaximumValue.get(nation).textProperty().bind(viewModel.getMaxSteps());
        regionCurrentValue.get(nation).textProperty().bind(viewModel.getCurrentSteps());

        airfields.get(nation).itemsProperty().bind(viewModel.getAirfields());

        return this;
    }

    /**
     * Bind the view to the airfield view model.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param viewModel The airfield view model.
     * @return This squadron view.
     */
    public SquadronView bind(final Nation nation, final AirfieldViewModel viewModel) {
        airfieldMaximumValue.get(nation).textProperty().bind(viewModel.getMaxCapacity());
        airfieldCurrentValue.get(nation).textProperty().bind(viewModel.getCurrent());
        airfieldAntiAirValue.get(nation).textProperty().bind(viewModel.getAntiAir());

        airfieldSquadrons.get(nation).itemsProperty().bind(viewModel.getAirfieldSquadrons());
        availableSquadrons.get(nation).itemsProperty().bind(viewModel.getAvailableSquadrons());

        squadronAirfieldLabel.get(nation).textProperty().bind(viewModel.getAvailableSquadronsTitle());

        airfieldSteps.get(nation).forEach((type, label) -> label.textProperty().bind(viewModel.getAirfieldSteps().get(type)));

        return this;
    }

    /**
     * Bind the deployment.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param viewModel The deployment view model.
     * @return This squadron view object.
     */
    public SquadronView bind(final Nation nation, final DeploymentViewModel viewModel) {
        TableView<Deployment> table = deploymentStats.get(nation);

        table.itemsProperty().bind(viewModel.getDeployment().get(nation));

        IntegerProperty numRows = viewModel.getNumLandingTypes().get(nation);

        int headerSize = props.getInt("squadron.summary.table.cell.header.size");

        table.prefHeightProperty()
                .bind(Bindings.createDoubleBinding(() -> numRows.getValue() * table.getFixedCellSize() + headerSize, numRows));

        table.minHeightProperty().bind(table.prefHeightProperty());
        table.maxHeightProperty().bind(table.prefHeightProperty());


        Map<Region, BooleanProperty> regionMap = viewModel.getRegionMinimum().get(nation);

        regionDeployedValue
                .get(nation)
                .forEach((region, image) -> image.imageProperty()
                        .bind(Bindings.createObjectBinding(() -> imageMap.get(regionMap.get(region).getValue()),
                               regionMap.get(region))));

        return this;
    }

    /**
     * Show the task forces summary view.
     *
     * @param stage The stage on which the task force scene is set.
     * @param scenario The selected scenario.
     */
    public void show(final Stage stage, final Scenario scenario) {
        Node titlePane = buildTitle(scenario);
        Node objectivesPane = buildObjectives(scenario);
        Node nationTabPane = buildNationTabs();
        Node pushButtons = buildPushButtons();

        VBox vBox = new VBox(titlePane, objectivesPane, nationTabPane, pushButtons);

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
        Nation nation = dto.getNation();
        previewMap.get(nation).markAirfield(dto);
    }

    /**
     * Mark a squadron's range radius on the preview map.
     *
     * @param dto The data transfer object.
     */
    public void markSquadronRangeOnMap(final AssetMarkerDTO dto) {
        Nation nation = dto.getNation();
        previewMap.get(nation).markRange(dto);
    }

    /**
     * Clear a squadron's range radius on the preview map.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    public void clearSquadronRange(final Nation nation) {
        previewMap.get(nation).clearRange();
    }

    /**
     * Set the selected airfield. Show this airfield's map marker and details.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @param airfield The selected airfield.
     */
    public void setSelectedAirfield(final Nation nation, final Airfield airfield) {
        previewMap.get(nation).selectAirfieldMarker(airfield.getName());
    }

    /**
     * Clear an airfield marker.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airfield the airfield whose marker is cleared.
     */
    public void clearAirfield(final Nation nation, final Airfield airfield) {
        previewMap.get(nation).clearAirfieldMarker(airfield.getName());
    }

    /**
     * Finish the task force preview map.
     */
    public void finish() {
        nations.forEach(nation -> previewMap.get(nation).finish());
    }

    /**
     * Close the popup.
     *
     * @param nation The nation BRITISH, ITALIAN, etc...
     * @param event the mouse event.
     */
    public void closePopup(final Nation nation, final MouseEvent event) {
        previewMap.get(nation).closePopup(event);
    }

    /**
     * Build the minimum region map.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void buildMinimumRegion(final Nation nation) {
        Map<Region, ImageView> deployedMap = getRegionsWithMinimum(nation)
                .stream()
                .collect(Collectors.toMap(
                        region -> region,
                        region -> new ImageView()));


        regionDeployedValue.put(nation, deployedMap);
    }

    /**
     * Get the regions with minimum squadron requirements.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The regions that have a minimum squadron requirement.
     */
    private List<Region> getRegionsWithMinimum(final Nation nation) {
        return gameMap
                .getNationRegions(game.getHumanSide(), nation)
                .stream()
                .filter(Region::hasMinimumRequirement)
                .collect(Collectors.toList());
    }

    /**
     * Build the title pane.
     *
     * @param scenario The selected scenario.
     * @return A node containing the title.
     */
    private Node buildTitle(final Scenario scenario) {
        Label title = new Label("Squadrons: " + scenario.getTitle());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane");

        return titlePane;
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
                .map(props::getString)
                .map(flagImage -> imageResourceProvider.getImageView(scenario.getName(), flagImage))
                .collect(Collectors.toList());

        HBox hBox = new HBox();
        hBox.getChildren().addAll(nationFlags);
        hBox.getChildren().addAll(objectiveLabel, objectiveValue);
        hBox.setId("objective-pane");

        return hBox;
    }

    /**
     * Build the nations tab pane.
     *
     * @return The nations tab pane.
     */
    private Node buildNationTabs() {
        nationsTabPane = new TabPane();
        nationsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

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
        ChoiceBox<Region> regionChoiceBox = regions.get(nation);
        regionChoiceBox.getItems().addAll(gameMap.getNationRegions(side, nation));
        regionChoiceBox.setMinWidth(props.getInt("taskForce.details.width"));
        regionChoiceBox.setMaxWidth(props.getInt("taskForce.details.width"));

        Node regionDetails = buildRegionDetails(nation);

        VBox regionVBox = new VBox(regionLabel, regionChoiceBox, regionDetails);

        Label airfieldLabel = new Label("Airfield:");
        ChoiceBox<Airfield> airfieldChoiceBox = airfields.get(nation);
        airfieldChoiceBox.setMinWidth(props.getInt("taskForce.details.width"));
        airfieldChoiceBox.setMaxWidth(props.getInt("taskForce.details.width"));
        VBox airfieldVBox = new VBox(airfieldLabel, airfieldChoiceBox);

        Accordion accordion = new Accordion();
        TitledPane airfieldDetails = buildAirfieldDetails(nation);
        TitledPane squadronSummary = buildSquadronSummary(nation);
        accordion.getPanes().addAll(airfieldDetails, squadronSummary);
        accordion.setExpandedPane(airfieldDetails);

        VBox leftVBox = new VBox(regionVBox, airfieldVBox, accordion, buildLegend(nation), buildDeployment(nation),  buildRegionDeployment(nation));
        leftVBox.setId("squadron-vbox");
        leftVBox.setMinHeight(props.getInt("squadron.left.vbox.length"));

        Node mapNode = previewMap.get(nation).draw();
        Node squadrons = buildSquadronsPane(nation);

        VBox rightVBox = new VBox(mapNode, squadrons);

        HBox hBox = new HBox(leftVBox, rightVBox);
        hBox.setId("tab-pane");

        tab.setContent(hBox);

        ImageView roundel = imageResourceProvider.getImageView(props.getString(nation.toString() + ROUNDEL));

        tab.setGraphic(roundel);
        return tab;
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
        Text regionCurrentLabel = new Text("Region Current:");

        GridPane gridPane = new GridPane();
        gridPane.setId("region-details-grid");

        gridPane.add(regionMinimumLabel, 0, 0);
        gridPane.add(regionMinimumValue.get(nation), 1, 0);
        gridPane.add(regionMaximumLabel, 0, 1);
        gridPane.add(regionMaximumValue.get(nation), 1, 1);
        gridPane.add(regionCurrentLabel, 0, 2);
        gridPane.add(regionCurrentValue.get(nation), 1, 2);

        VBox vBox = new VBox(gridPane);
        vBox.setMinWidth(props.getInt("taskForce.details.width"));
        vBox.setMaxWidth(props.getInt("taskForce.details.width"));

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
        Text airfieldAntiAirLabel = new Text("AA Rating:");

        GridPane gridPane = new GridPane();
        gridPane.setId("airfield-details-grid");

        gridPane.add(airfieldMaximumLabel, 0, 0);
        gridPane.add(airfieldMaximumValue.get(nation), 1, 0);
        gridPane.add(airfieldCurrentLabel, 0, 1);
        gridPane.add(airfieldCurrentValue.get(nation), 1, 1);
        gridPane.add(airfieldAntiAirLabel, 0, 2);
        gridPane.add(airfieldAntiAirValue.get(nation), 1, 2);

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
        VBox vBox = new VBox(previewMap.get(nation).getLegendAirfield(nation));
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

        stats.setMaxWidth(props.getInt("taskForce.details.width"));
        stats.setMinWidth(props.getInt("taskForce.details.width"));

        VBox vBox = new VBox(label, stats);
        vBox.setId("deployment-vbox");

        return vBox;
    }


    /**
     * Build the region deployment.
     *
     * @param nation The nation BRITISH, ITALIAN, etc ...
     * @return The node containing the region deployment.
     */
    private Node buildRegionDeployment(final Nation nation) {
        List<Region> regionsWithMinimum = getRegionsWithMinimum(nation);

        VBox vBox = new VBox();

        if (regionsWithMinimum.size() > 0) {
            Map<Region, ImageView> deployedMap = regionDeployedValue.get(nation);

            GridPane gridPane = new GridPane();
            gridPane.setId("region-status-grid");

            int row = 0;
            for (Region region : regionsWithMinimum) {
                Label name = new Label(region.getName() + ":");
                ImageView deployed = deployedMap.get(region);
                name.setTooltip(new Tooltip("Indicates if region minimum squadron deployment satisfied"));

                gridPane.add(name, 0, row);
                gridPane.add(deployed, 1, row);

                row++;
            }

            Label label = new Label("Region Minimum Requirements:");
            VBox grid = new VBox(gridPane);
            vBox.getChildren().addAll(label, grid);
            grid.setId("region-status-vbox");
        }

        return vBox;
    }

    /**
     * Build the squadron pane.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The squadron pane Hbox.
     */
    private Node buildSquadronsPane(final Nation nation) {

        Button deployButton = deployButtons.get(nation);
        Button removeButton = removeButtons.get(nation);
        Button detailsButton = detailsButtons.get(nation);
        Label airfieldLabel = squadronAirfieldLabel.get(nation);

        ListView<Squadron> available = availableSquadrons.get(nation);
        ListView<Squadron> airfield = airfieldSquadrons.get(nation);

        available.setMinWidth(props.getInt("squadron.tabPane.width"));
        available.setMaxWidth(props.getInt("squadron.tabPane.width"));

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

        airfield.setMinWidth(props.getInt("squadron.tabPane.width"));
        airfield.setMaxWidth(props.getInt("squadron.tabPane.width"));

        Label availableLabel = new Label("Available Squadrons:");

        VBox availableVBox = new VBox(availableLabel, available);

        VBox airfieldVBox = new VBox(airfieldLabel, airfield);

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
