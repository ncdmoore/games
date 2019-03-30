package engima.waratsea.view.ships;

import com.google.inject.Inject;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ship details view.
 */
public class ShipDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properites.
     */
    @Inject
    public ShipDetailsView(final ImageResourceProvider imageResourceProvider,
                           final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the ship details view.
     *
     * @param ship The the ship to show.
     * @return A node that contains the ship details.
     */
    public Node show(final Ship ship) {
        Label title = new Label(getPrefix(ship) + ship.getName());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + ship.getSide().getPossesive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildShipTab(ship));
        tabPane.getTabs().add(buildStatusTab(ship));
        tabPane.getTabs().add(buildAircraftTab(ship));

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }

    /**
     * Build the ship tab.
     *
     * @param ship The ship whose specification is shown in the tab.
     * @return The ship tab.
     */
    private Tab buildShipTab(final Ship ship) {
        VBox shipVBox = new VBox(getImage(ship));
        shipVBox.setId("ship-image");

        TitledPane cargoPane = buildPane("Cargo", ship.getCargoData());
        cargoPane.setMinWidth(props.getInt("ship.dialog.detailsPane.width"));
        cargoPane.setMaxWidth(props.getInt("ship.dialog.detailsPane.width"));
        cargoPane.setExpanded(false);

        VBox detailsVBox = new VBox(shipVBox, buildShipDetails(ship), cargoPane);
        detailsVBox.setId("details-pane");

        Node componentsVBox = buildShipComponents(ship);
        componentsVBox.setId("components-pane");

        HBox hBox = new HBox(detailsVBox, componentsVBox);
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

        gridPane.setId("status-pane");

        Tab statusTab = new Tab("Status");
        statusTab.setClosable(false);
        statusTab.setContent(gridPane);
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
        Label values = new Label(component.getHealth() + "/" + maxHealth);
        ProgressBar progressBar = new ProgressBar(percent);
        progressBar.setMaxWidth(300);
        progressBar.setMinWidth(300);
        progressBar.getStyleClass().add("green-bar");

        return new ArrayList<>(Arrays.asList(titleLabel, progressBar, values));
    }

    /**
     * Build the ship's aircraft tab.
     *
     * @param ship The ship whose aircraft informatin is shown in the tab.
     * @return The aircraft tab.
     */
    private Tab buildAircraftTab(final Ship ship) {
        HBox hBox = new HBox(new Label("aircraft stuff"));

        Tab aircraftTab = new Tab("Aircraft");
        aircraftTab.setClosable(false);
        aircraftTab.setContent(hBox);

        return aircraftTab;
    }

    /**
     * Build the ship details pane.
     *
     * @param ship The ship.
     * @return The node that contains the ship's details.
     */
    private TitledPane buildShipDetails(final Ship ship) {
        final int row3 = 3;
        final int row4 = 4;

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(new Label(ship.getName()), 1, 0);
        gridPane.add(new Label("Type:"), 0, 1);
        gridPane.add(new Label(ship.getType().toString()), 1, 1);
        gridPane.add(new Label("Class:"), 0, 2);
        gridPane.add(new Label(ship.getShipClass()), 1, 2);
        gridPane.add(new Label("Nationality:"), 0, row3);
        gridPane.add(new Label(ship.getNationality().toString()), 1, row3);
        gridPane.add(new Label("Victory Points:"), 0, row4);
        gridPane.add(new Label(ship.getVictoryPoints() + ""), 1, row4);
        gridPane.setId("ship-details-grid");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Detials");
        titledPane.setContent(gridPane);
        titledPane.setCollapsible(false);

        titledPane.setMinWidth(props.getInt("ship.dialog.detailsPane.width"));
        titledPane.setMaxWidth(props.getInt("ship.dialog.detailsPane.width"));

        return titledPane;
    }

    /**
     * Build the ship components.
     *
     * @param ship The ship.
     * @return The node that contains the ship component's.
     */
    private Node buildShipComponents(final Ship ship) {
        TitledPane surfaceWeaponsPane = buildPane("Surface Weapons", ship.getSurfaceWeaponData());
        TitledPane antiAirWeaponsPane = buildPane("Anti-Air Weapons", ship.getAntiAirWeaponData());
        TitledPane torpedoPane = buildPane("Torpedos", ship.getTorpedoData());
        TitledPane armourPane = buildPane("Armour", ship.getArmourData());
        TitledPane speedPane = buildPane("Movement", ship.getMovementData());
        TitledPane fuelPane = buildPane("Fuel", ship.getFuelData());
        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane, speedPane, fuelPane);
    }

    /**
     * Build the component titled pane.
     *
     * @param title The title of the pane.
     * @param data The data contained within the pane.
     * @return The titled pane.
     */
    private TitledPane buildPane(final String title, final Map<String, String> data) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setContent(buildStats(data));
        pane.setMinWidth(props.getInt("ship.dialog.componentPane.width"));
        pane.setMaxWidth(props.getInt("ship.dialog.componentPane.width"));
        pane.setCollapsible(true);
        return pane;
    }

    /**
     * Build the weapons data.
     *
     * @param stats A map of the ship stats.
     * @return A grid of the weapons data.
     */
    private Node buildStats(final Map<String, String> stats) {
        GridPane gridPane = new GridPane();
        int i = 0;
        for (Map.Entry<String, String> entry : stats.entrySet()) {
            gridPane.add(new Label(entry.getKey()), 0, i);
            gridPane.add(new Label(entry.getValue()), 1, i);
            i++;
        }

        gridPane.getStyleClass().add("component-grid");
        return gridPane;
    }

    /**
     * Get the ship prefix.
     *
     * @param ship The ship.
     * @return The ship's prefix.
     */
    private String getPrefix(final Ship ship) {
        return ship.getNationality().getShipPrefix() + " ";
    }

    /**
     * Get the ship's image.
     *
     * @param ship The ship.
     * @return The ship's image view.
     */
    private ImageView getImage(final Ship ship) {
        return imageResourceProvider.getShipImageView(ship);
    }
}
