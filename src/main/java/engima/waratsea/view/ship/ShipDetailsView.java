package engima.waratsea.view.ship;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronDetailsView;
import engima.waratsea.view.util.BoundTitledGridPane;
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
    private final ViewProps props;

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

    private final ImageView shipImage = new ImageView();
    private final ImageView shipProfileImage = new ImageView();

    private final SquadronViewModel squadronViewModel;
    private final SquadronDetailsView squadronDetailsView;


    @Getter
    private final ChoiceBox<Squadron> squadrons = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param props View properties.
     * @param squadronViewModelProvider Provides the squadron view model.
     * @param squadronDetailsViewProvider Provides the squadron details view.
     */
    @Inject
    public ShipDetailsView(final ViewProps props,
                           final Provider<SquadronViewModel> squadronViewModelProvider,
                           final Provider<SquadronDetailsView> squadronDetailsViewProvider) {
        this.props = props;

        this.squadronViewModel = squadronViewModelProvider.get();
        this.squadronDetailsView = squadronDetailsViewProvider.get();
    }

    /**
     * Show the ship details view.
     *
     * @param ship The the ship to show.
     * @return A node that contains the ship details.
     */
    public Node build(final Ship ship) {
        Label title = new Label(getPrefix(ship) + ship.getTitle());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + ship.getSide().getPossessive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildShipTab());
        tabPane.getTabs().add(buildStatusTab(ship));
        if (ship.hasAircraft()) {
            tabPane.getTabs().add(buildAircraftTab());
        }

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
        shipVBox.setId("ship-image");

        buildPane(shipDetailsPane).setTitle("Ship Details");

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
     * @param ship THe ship whose status is shown in the tab.
     * @return The ship's status tab.
     */
    private Tab buildStatusTab(final Ship ship) {

        GridPane gridPane = new GridPane();
        gridPane.setId("status-grid");

        List<List<Node>> progressBars = ship
                .getComponents()
                .stream()
                .map(this::buildProgressBar)
                .collect(Collectors.toList());

        for (int row = 0; row < progressBars.size(); row++) {
            for (int column = 0; column < progressBars.get(row).size(); column++) {
                gridPane.add(progressBars.get(row).get(column), column, row);
            }
        }

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Status");
        titledPane.setContent(gridPane);
        titledPane.setExpanded(true);
        titledPane.setCollapsible(false);
        titledPane.setId("status-pane");

        Tab statusTab = new Tab("Status");
        statusTab.setClosable(false);
        statusTab.setContent(titledPane);
        return statusTab;
    }

    /**
     * Build the progress bar for a ship component.
     *
     * @param component The ship's component.
     * @return The node that contains the progress bar.
     */
    private ArrayList<Node> buildProgressBar(final Component component) {
        int maxHealth = component.getMaxHealth();

        Label titleLabel = new Label(component.getName() + ":");

        double percent = component.getHealth() * 1.0 / maxHealth;
        Label values = new Label(component.getHealth() + "/" + maxHealth + " " + component.getUnits());
        ProgressBar progressBar = new ProgressBar(percent);
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

        Tab aircraftTab = new Tab("Aircraft");
        aircraftTab.setClosable(false);
        aircraftTab.setContent(vBox);

        bindAircraftTab();

        return aircraftTab;
    }

    /**
     * Bind the aircraft data to the view.
     */
    private void bindAircraftTab() {
        squadronDetailsView.bind(squadronViewModel);
        squadronViewModel.getSquadron().bind(squadrons.getSelectionModel().selectedItemProperty());
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
        buildPane(surfaceWeaponsPane).setTitle("Surface Weapons");
        buildPane(antiAirWeaponsPane).setTitle("Anti-Air Weapons");
        buildPane(torpedoPane).setTitle("Torpedoes");
        buildPane(armourPane).setTitle("Armour");
        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane);
    }

    /**
     * Build the ship performance components.
     *
     * @return The node that contains the ship components.
     */
    private Node buildPerformance() {
        buildPane(performancePane).setTitle("Movement:");
        buildPane(aswPane).setTitle("ASW");
        buildPane(fuelPane).setTitle("Fuel");
        buildPane(squadronPane).setTitle("Aircraft");
        buildPane(cargoPane).setTitle("Cargo");
        return new VBox(performancePane, aswPane, fuelPane, squadronPane, cargoPane);
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     * @return The built pane.
     */
    private BoundTitledGridPane buildPane(final BoundTitledGridPane pane) {
        return pane.setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setGridStyleId("component-grid")
                .build();
    }

    /**
     * Get the ship prefix.
     *
     * @param ship The ship.
     * @return The ship's prefix.
     */
    private String getPrefix(final Ship ship) {
        return ship.getNation().getShipPrefix() + " ";
    }
}
