package engima.waratsea.view.submarine;

import com.google.inject.Inject;
import engima.waratsea.model.ship.Component;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.submarine.Submarine;
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
 * The submarine details view.
 */
@Slf4j
public class SubmarineDetailsView {
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
    public SubmarineDetailsView(final ResourceProvider resourceProvider,
                                final ViewProps props) {
        this.resourceProvider = resourceProvider;
        this.props = props;
    }

    /**
     * Show the submarine details view.
     *
     * @param submarine The the submarine to show.
     * @return A node that contains the submarine details.
     */
    public Node show(final Submarine submarine) {
        Label title = new Label(getPrefix(submarine) + submarine.getName());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + submarine.getSide().getPossessive().toLowerCase());

        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(buildSubTab(submarine));
        tabPane.getTabs().add(buildStatusTab(submarine));

        VBox mainPane = new VBox(titlePane, tabPane);
        mainPane.setId("main-pane");

        return mainPane;
    }


    /**
     * Build the submarine tab.
     *
     * @param submarine The submarine whose specification is shown in the tab.
     * @return The submarine tab.
     */
    private Tab buildSubTab(final Submarine submarine) {
        VBox shipVBox = new VBox(getImage(submarine));
        shipVBox.setId("ship-image");

        VBox detailsVBox = new VBox(shipVBox);
        detailsVBox.setId("details-pane");

        VBox weaponComponentsVBox = buildWeapons(submarine);
        weaponComponentsVBox.setId("components-pane");

        HBox leftHBox = new HBox(detailsVBox, weaponComponentsVBox);
        leftHBox.setId("left-hbox");

        Node profileBox = buildProfile(submarine);

        VBox leftVBox = new VBox(leftHBox, profileBox);
        leftVBox.setId("left-vbox");

        Node performanceComponetsVBox = buildPerformance(submarine);
        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(leftVBox, performanceComponetsVBox);
        hBox.setId("main-hbox");

        Tab shipTab = new Tab("Specifications");
        shipTab.setClosable(false);
        shipTab.setContent(hBox);

        return shipTab;
    }

    /**
     * Build the submarine's status.
     *
     * @param submarine The submarine whose status is shown in the tab.
     * @return The submarine's status tab.
     */
    private Tab buildStatusTab(final Submarine submarine) {

        GridPane gridPane = new GridPane();
        gridPane.setId("status-grid");

        List<List<Node>> progressBars = submarine
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
        titledPane.setText("Submarine Status");
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
     * Build the submarine's profile image.
     *
     * @param submarine The submarine whose profile is built.
     * @return The node that contains the submarine's profile image.
     */
    private Node buildProfile(final Submarine submarine) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Ship Profile");

        VBox shipVBox = new VBox(getProfileImage(submarine));
        shipVBox.setId("profile-vbox");

        titledPane.setContent(shipVBox);

        return titledPane;
    }

    /**
     * Build the submarine weapon components.
     *
     * @param submarine The submarine.
     * @return The node that contains the submarine components.
     */
    private VBox buildWeapons(final Submarine submarine) {
        TitledPane detailsPane = buildPane("Submarine Details", getSubDetailsData(submarine));
        TitledPane torpedoPane = buildPane("Torpedos", getTorpedoData(submarine));
        TitledPane fuelPane =  buildPane("Fuel", getFuelData(submarine));
        return new VBox(detailsPane, torpedoPane, fuelPane);
    }

    /**
     * Build the submarine performance components.
     *
     * @param submarine The submarine.
     * @return The node that contains the submarine components.
     */
    private Node buildPerformance(final Submarine submarine) {
        TitledPane speedPane = buildPane("Movement", getMovementData(submarine));
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
     * Get the submarine's surface weapon data.
     *
     * @param submarine The submarine whose surface weapon data is retreived.
     * @return A map of the submarine's details data.
     */
    private Map<String, String> getSubDetailsData(final Submarine submarine) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Name:", submarine.getName());
        details.put("Class:", submarine.getShipClass());
        details.put("Nationality:", submarine.getNationality().toString());
        details.put("Victory Points:", submarine.getVictoryPoints() + "");
        return details;
    }

    /**
     * Get the submarine's torpedo data.
     *
     * @param submarine The submarine whose torpedo data is retrieved.
     * @return A map of the submarine's torpedo data.
     */
    private Map<String, String> getTorpedoData(final Submarine submarine) {
        Map<String, String> weapons = new LinkedHashMap<>();
        weapons.put("Torpeodo:", submarine.getTorpedo().getHealth() + "");
        return weapons;
    }


    /**
     * Get the submarine's movement data.
     *
     * @param submarine The submarine whose movement is retrieved.
     * @return A map of the movement per turn type.
     */
    private Map<String, String> getMovementData(final Submarine submarine) {
        Map<String, String> speed = new LinkedHashMap<>();
        speed.put("Even turns:", submarine.getMovement().getEven() + "");
        speed.put("Odd turns:", submarine.getMovement().getOdd() + "");
        speed.put("", "");
        speed.put(" ", "");
        return speed;
    }

    /**
     * Get the ship's fuel data.
     *
     * @param submarine The submarine whose fuel data is retrieved.
     * @return The submarine's fuel data.
     */
    private Map<String, String> getFuelData(final Submarine submarine) {
        Map<String, String> fueldata = new LinkedHashMap<>();
        fueldata.put("Remaing Fuel:", submarine.getFuel().getLevel() + "");
        return fueldata;
    }


    /**
     * Get the submarine's prefix.
     *
     * @param submarine The submarine.
     * @return The submarine's prefix.
     */
    private String getPrefix(final Submarine submarine) {
        return submarine.getNationality().getShipPrefix() + " ";
    }

    /**
     * Get the submarine's image.
     *
     * @param submarine The submarine.
     * @return The submarine's image view.
     */
    private ImageView getImage(final Submarine submarine) {
        return resourceProvider.getShipImageView(submarine);
    }

    /**
     * Get the ship's profile image.
     *
     * @param submarine The submarine.
     * @return The submarine's profile image view.
     */
    private ImageView getProfileImage(final Submarine submarine) {
        return resourceProvider.getShipProfileImageView(submarine);
    }
}
