package engima.waratsea.view.ship;

import com.google.inject.Inject;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronDetailsView;
import engima.waratsea.view.util.TitledGridPane;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static engima.waratsea.model.squadron.StepSize.ONE_THIRD;
import static engima.waratsea.model.squadron.StepSize.TWO_THIRDS;

/**
 * The ship details view.
 */
@Slf4j
public class ShipDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private SquadronDetailsView squadronDetailsView;

    @Getter
    private ChoiceBox<Squadron> squadrons = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param squadronDetailsView The squadron details view.
     */
    @Inject
    public ShipDetailsView(final ImageResourceProvider imageResourceProvider,
                           final ViewProps props,
                           final SquadronDetailsView squadronDetailsView) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
        this.squadronDetailsView = squadronDetailsView;
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
        squadronDetailsView.selectSquadron(squadron);
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

        Node weaponComponentsVBox = buildWeapons(ship);
        weaponComponentsVBox.setId("components-pane");

        HBox leftHBox = new HBox(detailsVBox, weaponComponentsVBox);
        leftHBox.setId("left-hbox");

        Node profileBox = buildProfile(ship);

        VBox leftVBox = new VBox(leftHBox, profileBox);
        leftVBox.setId("left-vbox");

        Node performanceComponetsVBox = buildPerformance(ship);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(leftVBox, performanceComponetsVBox);
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
     * @param ship The ship whose aircraft informatin is shown in the tab.
     * @return The aircraft tab.
     */
    private Tab buildAircraftTab(final Ship ship) {

        squadrons.getItems().clear();
        squadrons.getItems().addAll(ship.getSquadrons());

        VBox aircraftListBox = new VBox(new Label("Select Squadron:"), squadrons);
        aircraftListBox.setId("aircraft-list");

        Squadron squadron = squadrons.getItems().get(0);

        Node imageBox = squadronDetailsView.buildImage(squadron);
        imageBox.setId("ship-image");

        Node profileBox = squadronDetailsView.buildProfile(squadron);


        VBox listBox = new VBox(imageBox, profileBox);
        listBox.setId("details-pane");

        Node weaponComponentsVBox = squadronDetailsView.buildWeapons(squadron);
        weaponComponentsVBox.setId("components-pane");

        Node performanceComponetsVBox = squadronDetailsView.buildPerformance(squadron);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(listBox, weaponComponentsVBox, performanceComponetsVBox);
        hBox.setId("main-hbox");

        VBox vBox = new VBox(aircraftListBox, hBox);

        Tab aircraftTab = new Tab("Aircraft");
        aircraftTab.setClosable(false);
        aircraftTab.setContent(vBox);

        return aircraftTab;
    }

    /**
     * Build the ship's profile image.
     *
     * @param ship The ship whose profile is built.
     * @return The node that contains the ship's profile image.
     */
    private Node buildProfile(final Ship ship) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Profile");

        VBox shipVBox = new VBox(getProfileImage(ship));
        shipVBox.setId("profile-vbox");

        titledPane.setContent(shipVBox);

        return titledPane;
    }

    /**
     * Build the ship weapon components.
     *
     * @param ship The ship.
     * @return The node that contains the ship components.
     */
    private Node buildWeapons(final Ship ship) {
        TitledGridPane surfaceWeaponsPane = buildPane("Surface Weapons", getSurfaceWeaponData(ship));
        TitledGridPane antiAirWeaponsPane = buildPane("Anti-Air Weapons", getAntiAirWeaponData(ship));
        TitledGridPane torpedoPane = buildPane("Torpedos", getTorpedoData(ship));
        TitledGridPane armourPane = buildPane("Armour", getArmourData(ship));
        return new VBox(surfaceWeaponsPane, antiAirWeaponsPane, torpedoPane, armourPane);
    }

    /**
     * Build the ship performance components.
     *
     * @param ship The ship.
     * @return The node that contains the ship components.
     */
    private Node buildPerformance(final Ship ship) {
        TitledGridPane speedPane = buildPane("Movement", getMovementData(ship));
        TitledGridPane aswPane = buildPane("ASW", getAswData(ship));
        TitledGridPane fuelPane = buildPane("Fuel", getFuelData(ship));
        TitledGridPane squadronPane = buildPane("Aircraft", getSquadronSummary(ship));
        TitledGridPane cargoPane = buildPane("Cargo", getCargoData(ship));
        return new VBox(speedPane, aswPane, fuelPane, squadronPane, cargoPane);
    }

    /**
     * Build the component titled pane.
     *
     * @param title The title of the pane.
     * @param data The data contained within the pane.
     * @return The titled pane.
     */
    private TitledGridPane buildPane(final String title, final Map<String, String> data) {

        return new TitledGridPane()
                .setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setStyleId("component-grid")
                .setTitle(title)
                .buildPane(data);
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
        details.put("Nationality:", ship.getNation().toString());
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
     * Get the ship's ASW data.
     *
     * @param ship The ship's whose ASW data is retrieved.
     * @return A map of the ASW data.
     */
    private Map<String, String> getAswData(final Ship ship) {
        Map<String, String> asw = new LinkedHashMap<>();
        asw.put("ASW Capable:", ship.getAsw().isAsw() + "");
        return asw;
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
     * Get a summary of the ship's squadrons by type of aircraft.
     *
     * @param ship The ship whose squadrons are retrieved.
     * @return A map of aircraft type to number of steps of that type.
     */
    private Map<String, String> getSquadronSummary(final Ship ship) {
         Map<String, String> summary = ship.getSquadronSummary()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toString() + ":",
                                          e -> formatSteps(e.getValue())));

         if (summary.isEmpty()) {
             summary.put("No aircraft", "");
         }

         return summary;
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
     * Get the ship's profile image.
     *
     * @param ship The ship.
     * @return The ship's profile image view.
     */
    private ImageView getProfileImage(final Ship ship) {
        return imageResourceProvider.getShipProfileImageView(ship);
    }

    /**
     * Format the aircraft type steps.
     *
     * @param steps The number of steps of a given aircraft type.
     * @return A string value that represents the total number of steps of the aircraft type.
     */
    private String formatSteps(final BigDecimal steps) {

        String stepString = steps + "";

        log.info(stepString);

        BigDecimal oneThird = new BigDecimal(ONE_THIRD);
        BigDecimal twoThirds = new BigDecimal(TWO_THIRDS);

        if (steps.compareTo(BigDecimal.ZERO) > 0 && steps.compareTo(oneThird) <= 0) {
            return "1/3 of a step";
        } else if (steps.compareTo(oneThird) > 0 && steps.compareTo(twoThirds) <= 0) {
            return "2/3 of a step";
        } else if (steps.compareTo(BigDecimal.ONE) == 0) {
            return stepString + " step";
        } else {
            return stepString + " steps";
        }
    }
}
