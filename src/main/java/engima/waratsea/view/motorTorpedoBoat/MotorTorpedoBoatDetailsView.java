package engima.waratsea.view.motorTorpedoBoat;

import com.google.inject.Inject;
import engima.waratsea.model.motorTorpedoBoat.MotorTorpedoBoat;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ResourceProvider;
import engima.waratsea.view.ViewProps;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * The MTB details view.
 */
@Slf4j
public class MotorTorpedoBoatDetailsView {
    private final ResourceProvider resourceProvider;
    private final ViewProps props;

    @Getter
    private final ChoiceBox<Squadron> squadrons = new ChoiceBox<>();

    /**
     * Constructor called by guice.
     *
     * @param resourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public MotorTorpedoBoatDetailsView(final ResourceProvider resourceProvider,
                                       final ViewProps props) {
        this.resourceProvider = resourceProvider;
        this.props = props;
    }

    /**
     * Show the submarine details view.
     *
     * @param mtb The the MTB to show.
     * @return A node that contains the MTB details.
     */
    public Node show(final MotorTorpedoBoat mtb) {
        Label title = new Label(getPrefix(mtb) + mtb.getName());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + mtb.getSide().getPossessive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildSubTab(mtb));
        tabPane.getTabs().add(buildStatusTab(mtb));

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }


    /**
     * Build the MTB tab.
     *
     * @param mtb The MTB whose specification is shown in the tab.
     * @return The MTB tab.
     */
    private Tab buildSubTab(final MotorTorpedoBoat mtb) {
        VBox shipVBox = new VBox(getImage(mtb));
        shipVBox.setId("ship-image");

        VBox detailsVBox = new VBox(shipVBox);
        detailsVBox.setId("details-pane");

        VBox weaponComponentsVBox = buildWeapons(mtb);
        weaponComponentsVBox.setId("components-pane");

        HBox leftHBox = new HBox(detailsVBox, weaponComponentsVBox);
        leftHBox.setId("left-hbox");

        Node profileBox = buildProfile(mtb);

        VBox leftVBox = new VBox(leftHBox, profileBox);
        leftVBox.setId("left-vbox");

        Node performanceComponetsVBox = buildPerformance(mtb);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(leftVBox, performanceComponetsVBox);
        hBox.setId("main-hbox");

        Tab shipTab = new Tab("Specifications");
        shipTab.setClosable(false);
        shipTab.setContent(hBox);

        return shipTab;
    }

    /**
     * Build the MTB's status.
     *
     * @param mtb The MTB whose status is shown in the tab.
     * @return The MTB's status tab.
     */
    private Tab buildStatusTab(final MotorTorpedoBoat mtb) {

        GridPane gridPane = new GridPane();
        gridPane.setId("status-grid");

        List<List<Node>> progressBars = mtb
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
        titledPane.setText("Motor Torpedo Boat Status");
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
     * Build the MTB's profile image.
     *
     * @param mtb The MTB whose profile is built.
     * @return The node that contains the MTB's profile image.
     */
    private Node buildProfile(final MotorTorpedoBoat mtb) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Profile");

        VBox shipVBox = new VBox(getProfileImage(mtb));
        shipVBox.setId("profile-vbox");

        titledPane.setContent(shipVBox);

        return titledPane;
    }

    /**
     * Build the MTB weapon components.
     *
     * @param mtb The MTB.
     * @return The node that contains the mtb components.
     */
    private VBox buildWeapons(final MotorTorpedoBoat mtb) {
        TitledPane detailsPane = buildPane("Submarine Details", getSubDetailsData(mtb));
        TitledPane torpedoPane = buildPane("Torpedos", getTorpedoData(mtb));
        TitledPane fuelPane =  buildPane("Fuel", getFuelData(mtb));
        return new VBox(detailsPane, torpedoPane, fuelPane);
    }

    /**
     * Build the MTB performance components.
     *
     * @param mtb The mtb.
     * @return The node that contains the MTB components.
     */
    private Node buildPerformance(final MotorTorpedoBoat mtb) {
        TitledPane speedPane = buildPane("Movement", getMovementData(mtb));
        return new VBox(speedPane);
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
     * Get the MTB's surface weapon data.
     *
     * @param mtb The MTB whose surface weapon data is retreived.
     * @return A map of the MTB's details data.
     */
    private Map<String, String> getSubDetailsData(final MotorTorpedoBoat mtb) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Name:", mtb.getName());
        details.put("Class:", mtb.getShipClass());
        details.put("Nationality:", mtb.getNationality().toString());
        details.put("Victory Points:", mtb.getVictoryPoints() + "");
        return details;
    }

    /**
     * Get the MTB's torpedo data.
     *
     * @param mtb The MTB whose torpedo data is retrieved.
     * @return A map of the MTB's torpedo data.
     */
    private Map<String, String> getTorpedoData(final MotorTorpedoBoat mtb) {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Torpeodo:", mtb.getTorpedo().getHealth() + "");
        return weapons;
    }


    /**
     * Get the MTB's movement data.
     *
     * @param mtb The submarine whose movement is retrieved.
     * @return A map of the movement per turn type.
     */
    private Map<String, String> getMovementData(final MotorTorpedoBoat mtb) {
        Map<String, String> speed = new LinkedHashMap<>();
        speed.put("Even turns:", mtb.getMovement().getEven() + "");
        speed.put("Odd turns:", mtb.getMovement().getOdd() + "");
        speed.put("", "");
        speed.put(" ", "");
        return speed;
    }

    /**
     * Get the ship's fuel data.
     *
     * @param mtb The MTB whose fuel data is retrieved.
     * @return The MTB's fuel data.
     */
    private Map<String, String> getFuelData(final MotorTorpedoBoat mtb) {
        Map<String, String> fueldata = new LinkedHashMap<>();
        fueldata.put("Remaing Fuel:", mtb.getFuel().getLevel() + "");
        return fueldata;
    }


    /**
     * Get the MTB's prefix.
     *
     * @param mtb The MTB.
     * @return The MTB's prefix.
     */
    private String getPrefix(final MotorTorpedoBoat mtb) {
        return mtb.getNationality().getShipPrefix() + " ";
    }

    /**
     * Get the MTB's image.
     *
     * @param mtb The MTB.
     * @return The submarine's image view.
     */
    private ImageView getImage(final MotorTorpedoBoat mtb) {
        return resourceProvider.getShipImageView(mtb);
    }

    /**
     * Get the ship's profile image.
     *
     * @param mtb The MTB.
     * @return The MTB's profile image view.
     */
    private ImageView getProfileImage(final MotorTorpedoBoat mtb) {
        return resourceProvider.getShipProfileImageView(mtb);
    }
}
