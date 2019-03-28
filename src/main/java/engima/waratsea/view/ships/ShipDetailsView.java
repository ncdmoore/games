package engima.waratsea.view.ships;

import com.google.inject.Inject;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.utility.ImageResourceProvider;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * The ship details view.
 */
public class ShipDetailsView {
    private static final String EXTENSION = ".png";
    private final ImageResourceProvider imageResourceProvider;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     */
    @Inject
    public ShipDetailsView(final ImageResourceProvider imageResourceProvider) {
        this.imageResourceProvider = imageResourceProvider;
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
        tabPane.getTabs().add(buildAircraftTab(ship));

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }

    /**
     * Build the ship tab.
     *
     * @param ship The ship whose information is shown in the tab.
     * @return The ship tab.
     */
    private Tab buildShipTab(final Ship ship) {


        VBox shipVBox = new VBox(getImage(ship));
        shipVBox.setId("ship-image");

        VBox detailsVBox = new VBox(shipVBox, buildShipDetails(ship));
        detailsVBox.setId("details-pane");

        Node componentsVBox = buildShipComponents(ship);
        componentsVBox.setId("components-pane");

        HBox hBox = new HBox(detailsVBox, componentsVBox);
        hBox.setId("main-hbox");

        Tab shipTab = new Tab("Ship Information");
        shipTab.setClosable(false);
        shipTab.setContent(hBox);

        return shipTab;
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

        GridPane gridPane = new GridPane();
        gridPane.add(new Label("Name:"), 0, 0);
        gridPane.add(new Label(ship.getName()), 1, 0);
        gridPane.add(new Label("Type:"), 0, 1);
        gridPane.add(new Label(ship.getType().toString()), 1, 1);
        gridPane.add(new Label("Class:"), 0, 2);
        gridPane.add(new Label(ship.getShipClass()), 1, 2);
        gridPane.add(new Label("Nationality:"), 0, 3);
        gridPane.add(new Label(ship.getNationality().toString()), 1, 3);
        gridPane.setId("ship-details-grid");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Detials");
        titledPane.setContent(gridPane);
        titledPane.setCollapsible(false);

        titledPane.setMinWidth(302);
        titledPane.setMaxWidth(302);

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

        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane, speedPane);
    }

    private TitledPane buildPane(final String title, final Map<String, String> data) {
        TitledPane pane = new TitledPane();
        pane.setText(title);
        pane.setContent(buildStats(data));
        pane.setMinWidth(400);
        pane.setMaxWidth(400);
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

        return gridPane;
    }

    private String getPrefix(final Ship ship) {
        switch (ship.getNationality()) {
            case BRITISH:
                return "HMS ";
            case ITALIAN:
                return "RM ";
            case AUSTRALIAN:
                return "HMAS ";
            default:
                return "Unknown ";
        }
    }

    /**
     * Get the ship's image.
     *
     * @param ship The ship.
     * @return The ship's image view.
     */
    private ImageView getImage(final Ship ship) {
        //Look for an image for this specific ship.
        String name = ship.getName().replace(" ", "_");
        ImageView shipImage = imageResourceProvider.getImageView(name + EXTENSION);

        //If the specific ship image is not found use the ship's class image.
        if (shipImage.getImage() == null) {
            String shipClassName = ship.getShipClass().replace(" ", "_");
            shipImage = imageResourceProvider.getImageView(shipClassName + EXTENSION);
        }

        return shipImage;
    }
}
