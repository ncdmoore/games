package engima.waratsea.view.ship;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronDetailsView;
import engima.waratsea.view.util.BoundTitledGridPane;
import engima.waratsea.viewmodel.ship.ComponentViewModel;
import engima.waratsea.viewmodel.ship.ShipViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The ship details view.
 */
@Slf4j
public class ShipDetailsView {
    private final Game game;
    private final ViewProps props;

    private final Label title = new Label();
    private final BoundTitledGridPane shipDetailsPane = new BoundTitledGridPane();
    private final BoundTitledGridPane surfaceWeaponsPane = new BoundTitledGridPane();
    private final BoundTitledGridPane antiAirWeaponsPane = new BoundTitledGridPane();
    private final BoundTitledGridPane torpedoPane = new BoundTitledGridPane();
    private final BoundTitledGridPane armourPane = new BoundTitledGridPane();
    private final BoundTitledGridPane performancePane = new BoundTitledGridPane();
    private final BoundTitledGridPane aswPane = new BoundTitledGridPane();
    private final BoundTitledGridPane fuelPane = new BoundTitledGridPane();
    private final BoundTitledGridPane squadronPane = new BoundTitledGridPane();
    private final BoundTitledGridPane cargoPane = new BoundTitledGridPane();

    private final TitledPane statusPane = new TitledPane();

    private final Tab aircraftTab = new Tab("Aircraft");

    private final ImageView shipImage = new ImageView();
    private final ImageView shipProfileImage = new ImageView();

    private final SquadronViewModel squadronViewModel;
    private final SquadronDetailsView squadronDetailsView;

    @Getter
    private final ChoiceBox<Squadron> squadrons = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param props View properties.
     * @param squadronViewModelProvider Provides the squadron view model.
     * @param squadronDetailsViewProvider Provides the squadron details view.
     */
    @Inject
    public ShipDetailsView(final Game game,
                           final ViewProps props,
                           final Provider<SquadronViewModel> squadronViewModelProvider,
                           final Provider<SquadronDetailsView> squadronDetailsViewProvider) {
        this.game = game;
        this.props = props;

        this.squadronViewModel = squadronViewModelProvider.get();
        this.squadronDetailsView = squadronDetailsViewProvider.get();
    }

    /**
     * Show the ship details view.
     *
     * @return A node that contains the ship details.
     */
    public Node build() {
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + game.getHumanSide().getPossessive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildShipTab());
        tabPane.getTabs().add(buildStatusTab());
        tabPane.getTabs().add(buildAircraftTab());

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }

    /**
     * Bind to the view model.
     *
     * @param viewModel The ship view model.
     */
    public void bind(final ShipViewModel viewModel) {
        title.textProperty().bind(viewModel.getFullTitle());

        shipDetailsPane.bindStrings(viewModel.getShipDetailsData());
        surfaceWeaponsPane.bindStrings(viewModel.getSurfaceWeaponData());
        antiAirWeaponsPane.bindStrings(viewModel.getAntiAirWeaponData());
        torpedoPane.bindListStrings(viewModel.getTorpedoData());
        armourPane.bindStrings(viewModel.getArmourData());
        performancePane.bindStrings(viewModel.getMovementData());
        fuelPane.bindStrings(viewModel.getFuelData());
        aswPane.bindStrings(viewModel.getAswData());
        squadronPane.bindStrings(viewModel.getSquadronSummary());
        cargoPane.bindStrings(viewModel.getCargoData());

        shipImage.imageProperty().bind(viewModel.getShipImage());
        shipProfileImage.imageProperty().bind(viewModel.getShipProfileImage());

        bindStatusTab(viewModel);

        aircraftTab.disableProperty().bind(viewModel.getNoSquadrons());
        squadronDetailsView.bind(squadronViewModel);
        squadronViewModel.getSquadron().bind(squadrons.getSelectionModel().selectedItemProperty());

        squadrons.itemsProperty().bind(viewModel.getSquadrons());
        squadrons.getSelectionModel().selectFirst();                                                                    // Force the first squadron to be shown.
    }

    /**
     * Build the ship tab.
     *
     * @return The ship tab.
     */
    private Tab buildShipTab() {
        VBox shipVBox = new VBox(shipImage);
        shipVBox.setId("image");

        buildPane(shipDetailsPane, "Ship Details");

        VBox detailsVBox = new VBox(shipVBox, shipDetailsPane);
        detailsVBox.setId("details-pane");

        Node weaponComponentsVBox = buildWeapons();
        weaponComponentsVBox.getStyleClass().add("components-pane");

        HBox leftHBox = new HBox(detailsVBox, weaponComponentsVBox);
        leftHBox.setId("left-hbox");

        Node profileBox = buildProfile();

        VBox leftVBox = new VBox(leftHBox, profileBox);
        leftVBox.setId("left-vbox");

        Node performanceComponentsVBox = buildPerformance();
        performanceComponentsVBox.getStyleClass().add("components-pane");

        HBox hBox = new HBox(leftVBox, performanceComponentsVBox);
        hBox.setId("main-hbox");

        Tab shipTab = new Tab("Specifications");
        shipTab.setClosable(false);
        shipTab.setContent(hBox);

        return shipTab;
    }

    /**
     * Build the ship's status.
     *
     * @return The ship's status tab.
     */
    private Tab buildStatusTab() {
        statusPane.setText("Ship Status");
        statusPane.setExpanded(true);
        statusPane.setCollapsible(false);
        statusPane.setId("status-pane");

        Tab statusTab = new Tab("Status");
        statusTab.setClosable(false);
        statusTab.setContent(statusPane);
        return statusTab;
    }

    private void bindStatusTab(final ShipViewModel viewModel) {
        GridPane gridPane = new GridPane();
        gridPane.setId("status-grid");

        List<List<Node>> progressBars = viewModel
                .getComponents()
                .getValue()
                .stream()
                .map(this:: buildProgressBar)
                .collect(Collectors.toList());

        for (int row = 0; row < progressBars.size(); row++) {
            for (int column = 0; column < progressBars.get(row).size(); column++) {
                gridPane.add(progressBars.get(row).get(column), column, row);
            }
        }

        statusPane.setContent(gridPane);
    }

    /**
     * Build the progress bar for a ship component.
     *
     * @param component The ship's component.
     * @return The node that contains the progress bar.
     */
    private ArrayList<Node> buildProgressBar(final ComponentViewModel component) {
        Label titleLabel = new Label();
        titleLabel.textProperty().bind(component.getTitle());

        Label values = new Label();
        values.textProperty().bind(component.getValue());
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(component.getPercent());
        progressBar.setMaxWidth(props.getInt("ship.dialog.status.progressBar.width"));
        progressBar.setMinWidth(props.getInt("ship.dialog.status.progressBar.width"));
        progressBar.getStyleClass().add("green-bar");

        return new ArrayList<>(Arrays.asList(titleLabel, progressBar, values));
    }

    /**
     * Build the ship's aircraft tab.
     *
     * @return The aircraft tab.
     */
    private Tab buildAircraftTab() {
        VBox aircraftListBox = new VBox(new Label("Select Squadron:"), squadrons);
        aircraftListBox.setId("aircraft-list");

        Node aircraftBox = squadronDetailsView.build();

        VBox vBox = new VBox(aircraftListBox, aircraftBox);

        aircraftTab.setClosable(false);
        aircraftTab.setContent(vBox);

        return aircraftTab;
    }

    /**
     * Build the ship's profile image.
     *
     * @return The node that contains the ship's profile image.
     */
    private Node buildProfile() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Profile");

        VBox shipVBox = new VBox(shipProfileImage);
        shipVBox.setId("profile-vbox");

        titledPane.setContent(shipVBox);

        return titledPane;
    }

    /**
     * Build the ship weapon components.
     *
     * @return The node that contains the ship components.
     */
    private Node buildWeapons() {
        buildPane(surfaceWeaponsPane, "Surface Weapons");
        buildPane(antiAirWeaponsPane, "Anti-Air Weapons");
        buildPane(torpedoPane, "Torpedoes");
        buildPane(armourPane, "Armour");
        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane);
    }

    /**
     * Build the ship performance components.
     *
     * @return The node that contains the ship components.
     */
    private Node buildPerformance() {
        buildPane(performancePane, "Movement:");
        buildPane(aswPane, "ASW");
        buildPane(fuelPane, "Fuel");
        buildPane(squadronPane, "Aircraft Squadrons");
        buildPane(cargoPane, "Cargo");
        return new VBox(performancePane, aswPane, fuelPane, squadronPane, cargoPane);
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     * @param paneTitle The pane's title.
     */
    private void buildPane(final BoundTitledGridPane pane, final String paneTitle) {
         pane.setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setGridStyleId("component-grid")
                .build()
                .setTitle(paneTitle);
    }
}
