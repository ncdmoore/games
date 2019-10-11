package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.TitledGridPane;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SquadronSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private final Label title = new Label();

    private final VBox mainPane =  new VBox();
    private final ImageView aircraftProfile = new ImageView();
    private final TitledGridPane attackStats = new TitledGridPane();
    private final TitledGridPane performanceStats = new TitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public SquadronSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the squadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     *
     * @return A node containing the squadron summary.
     */
    public Node show(final Nation nation) {
        title.setId("summary-title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("summary-title-pane-" + nation.getFileName().toLowerCase());

        Node profile = buildProfile();

        buildStats();

        HBox hBox = new HBox(profile, attackStats, performanceStats);
        hBox.setId("summary-hbox");

        VBox vBox = new VBox(titlePane, hBox);
        vBox.setId("summary-vbox");

        mainPane.getChildren().add(vBox);
        mainPane.setVisible(false);
        mainPane.setId("summary-pane");

        return mainPane;
    }

    /**
     * Set the squadron for which the summary is shown.
     *
     * @param squadron The squadron who's summary is shown.
     */
    public void setSquadron(final Squadron squadron) {
        title.setText(squadron.getTitle());
        aircraftProfile.setImage(getProfile(squadron));
        attackStats.buildPaneMultiColumn(getAttackStats(squadron));
        performanceStats.buildPane(getPerformanceStats(squadron));
        mainPane.setVisible(true);
    }

    /**
     * Build the aircraft profile image.
     *
     * @return The node containing the aircraft profile image.
     */
    private Node buildProfile() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Profile");

        VBox vBox = new VBox(aircraftProfile);
        vBox.setId("profile-vbox");

        titledPane.setContent(vBox);
        titledPane.setMinHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMaxHeight(props.getInt("airfield.dialog.profile.height"));
        titledPane.setMinWidth(props.getInt("airfield.dialog.ready.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.ready.width"));
        return titledPane;
    }

    /**
     * Build the component titled panes.
     */
    private void buildStats() {
        buildAttacks();
        buildPerformance();
    }

    /**
     * Build the attack data.
     */
    private void buildAttacks() {
        attackStats
                .setWidth(props.getInt("airfield.dialog.ready.width"))
                .setStyleId("component-grid");

        ImageView imageView = imageResourceProvider.getImageView("info15x15.png");
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-attack-data-title");

        Tooltip tooltip = new Tooltip("Squadron Attack Data:\n\nFirst number is the attack factor.\nSecond number is the attack (modifier).\nThird number is the % chance of a single hit.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        attackStats.setGraphic(hbox);
        attackStats.setContentDisplay(ContentDisplay.RIGHT);
        attackStats.setText("Squadron Attack Data");
    }

    /**
     * Build the performance data.
     */
    private void buildPerformance() {
        performanceStats
                .setWidth(props.getInt("airfield.dialog.ready.width"))
                .setStyleId("component-grid");

        ImageView imageView = imageResourceProvider.getImageView("info15x15.png");
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-performance-data-title");

        Tooltip tooltip = new Tooltip("Squadron Performance Data:\n\nRadius is in grid squares.\nEndurance is in game turns.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        performanceStats.setGraphic(hbox);
        performanceStats.setContentDisplay(ContentDisplay.RIGHT);
        performanceStats.setText("Squadron Performance Data");
    }

    /**
     * Get the squadron attack data.
     *
     * @param squadron The selected squadron.
     * @return The squadron's attack data.
     */
    private Map<String, List<String>> getAttackStats(final Squadron squadron) {
        Map<String, List<String>> details = new LinkedHashMap<>();

        String airToAir = squadron.getAirModifier() > 0
                ? squadron.getAirFactor() + " (" + squadron.getAirModifier() + ")"
                : squadron.getAirFactor() + "";

        String land = squadron.getLandModifier() > 0
                ? squadron.getLandFactor() + " (" + squadron.getLandModifier() + ")"
                : squadron.getLandFactor() + "";

        String naval = squadron.getNavalModifier() > 0
                ? squadron.getNavalFactor() + " (" + squadron.getNavalModifier() + ")"
                : squadron.getNavalFactor() + "";

        List<String> strength = new ArrayList<>();
        strength.add(squadron.getStrength().toString());

        List<String> airToAirList = new ArrayList<>();
        airToAirList.add(airToAir);
        airToAirList.add(squadron.getAirHitProbability() + "%");

        List<String> landList = new ArrayList<>();
        landList.add(land);
        landList.add(squadron.getLandHitProbability() + "%");

        List<String> navalList = new ArrayList<>();
        navalList.add(naval);
        navalList.add(squadron.getNavalHitProbability() + "%");

        details.put("Strength:", strength);
        details.put(" ", new ArrayList<>());
        details.put("Air-to-Air Attack:", airToAirList);
        details.put("Land Attack:", landList);
        details.put("Naval Attack:", navalList);
        details.put("  ", new ArrayList<>());

        return details;
    }

    /**
     * Get the squadron performance data.
     *
     * @param squadron The selected squadron.
     * @return The squadron's performance data.
     */
    private Map<String, String> getPerformanceStats(final Squadron squadron) {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("Landing Type:", squadron.getAircraft().getLanding().toString());
        details.put("Altitude Rating:", squadron.getAircraft().getAltitude().toString());
        details.put(" ", "");
        details.put("Range:", squadron.getAircraft().getRadius().get(0) + "");

        if (squadron.getAircraft().getRadius().size() > 1) {
            details.put("Range (Drop Tanks):", squadron.getAircraft().getRadius().get(1) + "");
        } else {
            details.put("Range (Drop Tanks):", "Not Supported");
        }

        details.put("Endurance:", squadron.getAircraft().getRange().getEndurance() + "");

        return details;
    }

    /**
     * Get the aircraft's profile image.
     *
     * @param squadron The selected squadron.
     * @return The aircraft's profile image view.
     */
    private Image getProfile(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        return imageResourceProvider.getAircraftProfileImageView(squadron.getSide(), aircraft.getModel() + "-240");
    }
}
