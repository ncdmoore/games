package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AttackType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronFactor;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.utility.Probability;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the squadron summary. This is a component that can be included inside other views as a child view.
 *
 * Defined styles are:
 *
 *  summary-title-pane-{nation}
 *  summary-hbox
 *  summary-vbox
 *  summary-pane
 *  profile-vbox
 *  squadron-attack-data-title
 *  squadron-performance-data-title
 */
@Slf4j
public class SquadronSummaryView {
    @Getter private Squadron selectedSquadron;
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;
    private final Probability probability;
    private final Label title = new Label();
    private final VBox mainPane =  new VBox();
    private final ImageView aircraftProfile = new ImageView();
    private final TitledGridPane attackStats = new TitledGridPane();
    private final TitledGridPane performanceStats = new TitledGridPane();
    private SquadronConfig config;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param probability A probability utility.
     */
    @Inject
    public SquadronSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props,
                               final Probability probability) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
        this.probability = probability;
        this.config = SquadronConfig.NONE;
    }

    /**
     * Set the squadron configuration to display.
     *
     * @param newConfig A squadron configuration.
     * @return This object.
     */
    public SquadronSummaryView setConfig(final SquadronConfig newConfig) {
        config = newConfig;
        return  this;
    }

    /**
     * Show the selectedSquadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     *
     * @return A node containing the selectedSquadron summary.
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
     * Hide the summary.
     */
    public void hide() {
        mainPane.setVisible(false);
    }

    /**
     * Set the squadron for which the summary is shown.
     *
     * @param squadron The selectedSquadron who's summary is shown.
     */
    public void setSelectedSquadron(final Squadron squadron) {
        this.selectedSquadron = squadron;
        title.setText(selectedSquadron.getTitle());
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
        titledPane.setMinWidth(props.getInt("airfield.dialog.profile.width"));
        titledPane.setMaxWidth(props.getInt("airfield.dialog.profile.width"));
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
                .setWidth(props.getInt("airfield.dialog.profile.width"))
                .setStyleId("component-grid");

        ImageView imageView = imageResourceProvider.getImageView("info17x17.png");
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-attack-data-title");

        Tooltip tooltip = new Tooltip("Squadron Attack Data:\n\nFirst number is the attack factor.\nSecond number is the attack (modifier).\nD indicates Defensive Only.\nThird number is the % chance of a single hit.");

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
                .setWidth(props.getInt("airfield.dialog.profile.width"))
                .setStyleId("component-grid");

        ImageView imageView = imageResourceProvider.getImageView("info17x17.png");
        HBox hbox = new HBox(imageView);
        hbox.setId("squadron-performance-data-title");

        Tooltip tooltip = new Tooltip("Squadron Performance Data:\n\nRadius is in grid squares.\n"
                + "Endurance is in game turns.\nThe mission type may affect endurance.\n"
                + "If drop tanks are needed,\nthey are automatically equipped.");

        tooltip.setShowDelay(Duration.seconds(props.getDouble("tooltip.delay")));
        tooltip.setShowDuration(Duration.seconds(props.getDouble("tooltip.duration")));

        Tooltip.install(imageView, tooltip);

        performanceStats.setGraphic(hbox);
        performanceStats.setContentDisplay(ContentDisplay.RIGHT);
        performanceStats.setText("Squadron Performance Data");
    }

    /**
     * Get the selectedSquadron attack data.
     *
     * @param squadron The selected selectedSquadron.
     * @return The selectedSquadron's attack data.
     */
    private Map<String, List<String>> getAttackStats(final Squadron squadron) {
        Map<String, List<String>> details = new LinkedHashMap<>();

        SquadronFactor airFactor = squadron.getFactor(AttackType.AIR, config);

        String airToAir = airFactor.getModifier() != 0
                ? airFactor.getFactor() + " (" + airFactor.getModifier() + ")"
                : airFactor.isDefensive() ? airFactor.getFactor() + " (D)"
                : airFactor.getFactor() + "";

        SquadronFactor landFactor = squadron.getFactor(AttackType.LAND, config);

        String land = landFactor.getModifier() != 0
                ? landFactor.getFactor() + " (" + landFactor.getModifier() + ")"
                : landFactor.getFactor() + "";

        SquadronFactor navalFactor = squadron.getFactor(AttackType.NAVAL, config);

        String naval =  navalFactor.getModifier() != 0
                ? navalFactor.getFactor() + " (" + navalFactor.getModifier() + ")"
                : navalFactor.getFactor() + "";

        List<String> type = new ArrayList<>();
        type.add(squadron.getType().getAbbreviated());

        List<String> strength = new ArrayList<>();
        strength.add(squadron.getStrength().toString());

        List<String> airToAirList = new ArrayList<>();
        airToAirList.add(airToAir);
        airToAirList.add(probability.percentage(squadron.getHitProbability(AttackType.AIR, config)) + "%");

        List<String> landList = new ArrayList<>();
        landList.add(land);
        landList.add(probability.percentage(squadron.getHitProbability(AttackType.LAND, config)) + "%");

        List<String> navalList = new ArrayList<>();
        navalList.add(naval);
        navalList.add(probability.percentage(squadron.getHitProbability(AttackType.NAVAL, config)) + "%");

        details.put("Type:", type);
        details.put("Strength:", strength);
        details.put("  ", new ArrayList<>());
        details.put("Air-to-Air Attack:", airToAirList);
        details.put("Land Attack:", landList);
        details.put("Naval Attack:", navalList);

        return details;
    }

    /**
     * Get the selectedSquadron performance data.
     *
     * @param squadron The selected selectedSquadron.
     * @return The selectedSquadron's performance data.
     */
    private Map<String, String> getPerformanceStats(final Squadron squadron) {
        Map<String, String> details = new LinkedHashMap<>();

        details.put("Landing Type:", squadron.getAircraft().getLanding().toString());
        details.put("Altitude Rating:", squadron.getAircraft().getAltitude().toString());
        details.put(" ", "");
        details.put("Equipped:", config.toString());
        details.put("Radius:", squadron.getRadius(config) + "");
        details.put("Endurance:", squadron.getEndurance(config) + "");

        return details;
    }

    /**
     * Get the aircraft's profile image.
     *
     * @param squadron The selected selectedSquadron.
     * @return The aircraft's profile image view.
     */
    private Image getProfile(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        return imageResourceProvider.getAircraftProfileImageView(squadron.getSide(), aircraft.getModel() + "-240");
    }
}
