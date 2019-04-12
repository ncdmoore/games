package engima.waratsea.view.ships;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AttackFactor;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ship details view.
 */
public class ShipDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private ImageView aircraftImage = new ImageView();
    private TitledPane squadronDetailsPane;
    private TitledPane aircraftDetailsPane;
    private TitledPane aircraftAirToAirPane;
    private TitledPane aircraftLandPane;
    private TitledPane aircraftNavalPane;
    private TitledPane aircraftRangePane;
    private TitledPane aircraftFramePane;

    @Getter
    private ListView<Squadron> squadrons = new ListView<>();

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
        Label title = new Label(getPrefix(ship) + ship.getTitle());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + ship.getSide().getPossesive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildShipTab(ship));
        tabPane.getTabs().add(buildStatusTab(ship));
        if (ship.hasAircraft()) {
            tabPane.getTabs().add(buildAircraftTab(ship));
        }

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }

    /**
     * Select one of the ship's squadrons.
     *
     * @param squadron The selected squadron.
     */
    public void selectSquadron(final Squadron squadron) {
        aircraftImage.setImage(getImage(squadron));
        squadronDetailsPane.setContent(buildStats(getSquadronDetailsData(squadron)));
        aircraftDetailsPane.setContent(buildStats(getAircraftDetailsData(squadron)));
        aircraftAirToAirPane.setContent(buildStats(getAttackFactor(squadron, squadron.getAircraft().getAir())));
        aircraftLandPane.setContent(buildStats(getAttackFactor(squadron, squadron.getAircraft().getLand())));
        aircraftNavalPane.setContent(buildStats(getAttackFactor(squadron, squadron.getAircraft().getNaval())));
        aircraftRangePane.setContent(buildStats(getRange(squadron)));
        aircraftFramePane.setContent(buildStats(getFrame(squadron)));
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

        TitledPane shipDetailsPane = buildPane("Ship Details", getShipDetailsData(ship));

        VBox detailsVBox = new VBox(shipVBox, shipDetailsPane);
        detailsVBox.setId("details-pane");

        Node weaponComponentsVBox = buildShipWeaponComponents(ship);
        weaponComponentsVBox.setId("components-pane");

        Node performanceComponetsVBox = buildShipPerformanceComponents(ship);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(detailsVBox, weaponComponentsVBox, performanceComponetsVBox);
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


        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Status");
        titledPane.setContent(gridPane);
        titledPane.setExpanded(true);
        titledPane.setCollapsible(false);

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
        Label values = new Label(component.getHealth() + "/" + maxHealth);
        ProgressBar progressBar = new ProgressBar(percent);
        progressBar.setMaxWidth(props.getInt("ship.dialog.status.progressBar.width"));
        progressBar.setMinWidth(props.getInt("ship.dialog.status.progressBar.width"));
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

        squadrons.getItems().clear();
        squadrons.getItems().addAll(ship.getAircraft());
        squadrons.setMinHeight(props.getInt("ship.dialog.aircraft.list.height"));
        squadrons.setMaxHeight(props.getInt("ship.dialog.aircraft.list.height"));

        Squadron squadron = squadrons.getItems().get(0);

        aircraftImage.setImage(getImage(squadron));

        VBox imageBox = new VBox(aircraftImage);

        imageBox.setId("ship-image");

        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Squadrons");
        titledPane.setContent(squadrons);
        titledPane.setCollapsible(false);


        VBox listBox = new VBox(imageBox, titledPane);
        listBox.setId("details-pane");

        Node weaponComponentsVBox = buildSquadronWeaponComponents(squadron);
        weaponComponentsVBox.setId("components-pane");

        Node performanceComponetsVBox = buildSquadronPerformanceComponents(squadron);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(listBox, weaponComponentsVBox, performanceComponetsVBox);
        hBox.setId("main-hbox");

        Tab aircraftTab = new Tab("Aircraft");
        aircraftTab.setClosable(false);
        aircraftTab.setContent(hBox);

        return aircraftTab;
    }

    /**
     * Build the ship weapon components.
     *
     * @param ship The ship.
     * @return The node that contains the ship components.
     */
    private Node buildShipWeaponComponents(final Ship ship) {
        TitledPane surfaceWeaponsPane = buildPane("Surface Weapons", getSurfaceWeaponData(ship));
        TitledPane antiAirWeaponsPane = buildPane("Anti-Air Weapons", getAntiAirWeaponData(ship));
        TitledPane torpedoPane = buildPane("Torpedos", getTorpedoData(ship));
        TitledPane armourPane = buildPane("Armour", getArmourData(ship));
        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane);
    }

    /**
     * Build the ship performance components.
     *
     * @param ship The ship.
     * @return The node that contains the ship components.
     */
    private Node buildShipPerformanceComponents(final Ship ship) {
        TitledPane speedPane = buildPane("Movement", getMovementData(ship));
        TitledPane fuelPane = buildPane("Fuel", getFuelData(ship));
        TitledPane cargoPane = buildPane("Cargo", getCargoData(ship));
        return new VBox(speedPane, fuelPane, cargoPane);
    }

    /**
     * Build the squadron weapon components.
     *
     * @param squadron The squadron.
     * @return The node that contains the squadron components.
     */
    private Node buildSquadronWeaponComponents(final Squadron squadron) {
        squadronDetailsPane = buildPane("Squadron Details", getSquadronDetailsData(squadron));
        aircraftDetailsPane = buildPane("Aircraft Details", getAircraftDetailsData(squadron));
        aircraftLandPane = buildPane("Land", getAttackFactor(squadron, squadron.getAircraft().getLand()));
        aircraftNavalPane = buildPane("Naval", getAttackFactor(squadron, squadron.getAircraft().getNaval()));
        return new VBox(squadronDetailsPane, aircraftDetailsPane, aircraftLandPane, aircraftNavalPane);
    }

    /**
     * Build the squadron performance components.
     *
     * @param squadron The squadron.
     * @return The node that contains the squadron components.
     */
    private Node buildSquadronPerformanceComponents(final Squadron squadron) {
        aircraftAirToAirPane = buildPane("Air-to-Air", getAttackFactor(squadron, squadron.getAircraft().getAir()));
        aircraftRangePane = buildPane("Performance", getRange(squadron));
        aircraftFramePane = buildPane("Frame", getFrame(squadron));
        return new VBox(aircraftAirToAirPane, aircraftRangePane, aircraftFramePane);
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
        pane.setMinWidth(props.getInt("ship.dialog.detailsPane.width"));
        pane.setMaxWidth(props.getInt("ship.dialog.detailsPane.width"));
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
     * Get the ship's surface weapon data.
     *
     * @param ship The ship whose surface weapon data is retreived.
     * @return A map of the ship's surface weapon data.
     */
    private Map<String, String> getShipDetailsData(final Ship ship) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Name:", ship.getTitle());
        details.put("Type:", ship.getType().toString());
        details.put("Class:", ship.getShipClass());
        details.put("Nationality:", ship.getNationality().toString());
        details.put("Victory Points:", ship.getVictoryPoints() + "");
        details.put("", "");
        return details;
    }

    /**
     * Get the ship's surface weapon data.
     *
     * @param ship The ship whose surface weapon data is retreived.
     * @return A map of the ship's surface weapon data.
     */
    private Map<String, String> getSurfaceWeaponData(final Ship ship) {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Primary:", ship.getPrimary().getHealth() + "");
        weapons.put("Secondary:", ship.getSecondary().getHealth() + "");
        weapons.put("Tertiary:", ship.getTertiary().getHealth() + "");
        return weapons;
    }

    /**
     * Get the ship's anti air weapon data.
     *
     * @param ship The ship whose anti air weapon data is retrieved.
     * @return A map of the ship's anti-air weapon data.
     */
    private Map<String, String> getAntiAirWeaponData(final Ship ship) {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Anti Air:", ship.getAntiAir().getHealth() + "");
        return weapons;
    }

    /**
     * Get the ship's torpedo data.
     *
     * @param ship The ship whose torpedo data is retrieved.
     * @return A map of the ship's torpedo data.
     */
    private Map<String, String> getTorpedoData(final Ship ship) {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Torpeodo:", ship.getTorpedo().getHealth() + "");
        return weapons;
    }

    /**
     * Get the ship's armour data.
     *
     * @param ship The ship's whose armour data is retrieved.
     * @return A map of the armour type to armour value.
     */
    private Map<String, String> getArmourData(final Ship ship) {
        Map<String, String> armour = new LinkedHashMap<>();
        armour.put("Primary:", ship.getPrimary().getArmour().toString());
        armour.put("Secondary:", ship.getSecondary().getArmour().toString());
        armour.put("Tertiary:", ship.getTertiary().getArmour().toString());
        armour.put("Anti Air:", ship.getAntiAir().getArmour().toString());
        armour.put("Hull:", ship.getHull().getArmour().toString());
        armour.put("Deck:", ship.getHull().isDeck() + "");
        return armour;
    }

    /**
     * Get the ship's movement data.
     *
     * @param ship The ship whose movement is retrieved.
     * @return A map of the movement per turn type.
     */
    private Map<String, String> getMovementData(final Ship ship) {
        Map<String, String> speed = new LinkedHashMap<>();
        speed.put("Even turns:", ship.getMovement().getEven() + "");
        speed.put("Odd turns:", ship.getMovement().getOdd() + "");
        speed.put("", "");
        return speed;
    }

    /**
     * Get the ship's fuel data.
     *
     * @param ship The ship whose fuel data is retrieved.
     * @return The ship's fuel data.
     */
    private Map<String, String> getFuelData(final Ship ship) {
        Map<String, String> fueldata = new LinkedHashMap<>();
        fueldata.put("Remaing Fuel:", ship.getFuel().getLevel() + "");
        return fueldata;
    }

    /**
     * Get the ship's cargo data.
     *
     * @param ship The ship whose cargo data is retrieved
     * @return The ship's cargo data.
     */
    private Map<String, String> getCargoData(final Ship ship) {
        Map<String, String> cargoData = new LinkedHashMap<>();
        cargoData.put("Current Cargo:", ship.getCargo().getLevel() + "");
        return cargoData;
    }

    /**
     * Get the squadron details.
     *
     * @param squadron The selected squadron.
     * @return The squadron's details.
     */
    private Map<String, String> getSquadronDetailsData(final Squadron squadron) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Name:", squadron.getName());
        details.put("Strength:", squadron.getStrength() + "");
        return details;
    }

    /**
     * Get the aircraft details.
     *
     * @param squadron The selected squadron.
     * @return The aircraft's details.
     */
    private Map<String, String> getAircraftDetailsData(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Model:", aircraft.getModel());
        details.put("Type:", aircraft.getType() + "");
        details.put("Nationality:", aircraft.getNationality().toString());
        details.put("", "");
        return details;
    }

    /**
     * Get the squadron's attack factor data.
     *
     * @param squadron The selected squadron.
     * @param factor The attack factor.
     * @return The aircraft's attack data.
     */
    private Map<String, String> getAttackFactor(final Squadron squadron, final AttackFactor factor) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Factor", factor.getFactor(squadron.getStrength()) + "");
        details.put("Modifier", factor.getModifier() + "");
        return details;
    }

    /**
     * Get the squadron's range data.
     *
     * @param squadron The selected squadron.
     * @return The squadron's range data.
     */
    private Map<String, String> getRange(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Range", aircraft.getRange().getRange() + "");
        details.put("Endurance", aircraft.getRange().getEndurance() + "");
        details.put("Altitude Rating", aircraft.getAltitude().toString());
        details.put("Landing Type", aircraft.getLanding().toString());
        return details;
    }

    /**
     * Get the squadron's frame data.
     *
     * @param squadron The selected squadron.
     * @return The squadron's frame data.
     */
    private Map<String, String> getFrame(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Frame", aircraft.getFrame().getFrame() + "");
        details.put("Fragile", aircraft.getFrame().isFragile() + "");
        return details;
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

    /**
     * Get the aircraft's image.
     *
     * @param squadron The selected squadron.
     * @return The aircraft's image view.
     */
    private Image getImage(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        return imageResourceProvider.getAircraftImageView(aircraft);
    }
}
