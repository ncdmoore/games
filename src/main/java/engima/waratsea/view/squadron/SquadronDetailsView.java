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
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The detail view of a squadron.
 */
public class SquadronDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;
    private final Probability probability;

    private final Label title = new Label();
    private final ImageView aircraftImage = new ImageView();
    private final ImageView aircraftProfile = new ImageView();
    private final TitledGridPane squadronDetailsPane = new TitledGridPane();
    private final TitledGridPane aircraftDetailsPane = new TitledGridPane();
    private final TitledGridPane aircraftLandPane = new TitledGridPane();
    private final TitledGridPane aircraftNavalPane = new TitledGridPane();
    private final TitledGridPane aircraftAirToAirPane = new TitledGridPane();
    private final TitledGridPane aircraftPerformancePane = new TitledGridPane();
    private final TitledGridPane aircraftFramePane = new TitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     * @param probability Probability utility.
     */
    @Inject
    public SquadronDetailsView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props,
                               final Probability probability) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
        this.probability = probability;
    }

    /**
     * Show the squadron details.
     *
     * @param nation The nation.
     * @return A node containing the squadron details.
     */
    public Node build(final Nation nation) {
        Node titleBox = buildTitle(nation);
        Node imageBox = buildImages();
        Node weaponsBox = buildWeapons();
        Node performanceBox = buildPerformance();

        HBox hBox = new HBox(imageBox, weaponsBox, performanceBox);
        hBox.setId("main-hbox");

        VBox mainPane = new VBox(titleBox, hBox);

        mainPane.setId("main-pane");
        return mainPane;
    }

    /**
     * Set the selected squadron.
     *
     * @param squadron The selected squadron.
     * @param config The selected squadron's configuration.
     */
    public void setSquadron(final Squadron squadron, final SquadronConfig config) {
        title.setText(squadron.getTitle());
        aircraftImage.setImage(getImage(squadron));
        aircraftProfile.setImage(getProfile(squadron));

        squadronDetailsPane.updatePane(getSquadronDetailsData(squadron));
        aircraftDetailsPane.updatePane(getAircraftDetailsData(squadron));

        aircraftLandPane.updatePaneMultiColumn(getAttackFactor(
                squadron.getFactor(AttackType.LAND, config),
                squadron.getHitProbability(AttackType.LAND, config)));

        aircraftNavalPane.updatePaneMultiColumn(getAttackFactor(
                squadron.getFactor(AttackType.NAVAL, config),
                squadron.getHitProbability(AttackType.NAVAL, config)));

        aircraftAirToAirPane.updatePaneMultiColumn(getAttackFactor(
                squadron.getFactor(AttackType.AIR, config),
                squadron.getHitProbability(AttackType.AIR, config)));

        aircraftPerformancePane.updatePaneMultiColumn(getPerformance(squadron, config));
        aircraftFramePane.updatePane(getFrame(squadron));
    }

    /**
     * Build the title.
     *
     * @param nation The nation.
     * @return The title pane.
     */
    private Node buildTitle(final Nation nation) {
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + nation.getFileName().toLowerCase());

        return titlePane;
    }

    /**
     * Build the image boxes.
     *
     * @return A node containing the image nodes.
     */
    private Node buildImages() {
        Node imageBox = buildImage();
        Node profileBox = buildProfile();

        VBox vBox = new VBox(imageBox, profileBox);
        vBox.setId("image-vbox");

        return vBox;
    }

    /**
     * Build the squadron aircraft image.
     *
     * @return The node that contains the squadron image.
     */
    private Node buildImage() {
        VBox vBox = new VBox(aircraftImage);
        vBox.setId("squadron-image");
        return vBox;
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

        titledPane.getStyleClass().add("component-grid");
        titledPane.setContent(vBox);
        return titledPane;

    }

    /**
     * Build the aircraft's weapons boxes.
     *
     * @return The node containing the aircraft's weapons boxes.
     */
    private Node buildWeapons() {
        buildPane(squadronDetailsPane).setTitle("Squadron Details");
        buildPane(aircraftDetailsPane).setTitle("Aircraft Details");
        buildPane(aircraftLandPane).setTitle("Land Attack");
        buildPane(aircraftNavalPane).setTitle("Naval Attack");

        VBox vBox = new VBox(squadronDetailsPane, aircraftDetailsPane, aircraftLandPane, aircraftNavalPane);
        vBox.getStyleClass().add("components-pane");

        return vBox;
    }

    /**
     * Build the aircraft's performance boxes.
     *
     * @return The node containing the aircraft's performance boxes.
     */
    private Node buildPerformance() {
        buildPane(aircraftAirToAirPane).setTitle("Air Attack");
        buildPane(aircraftPerformancePane).setTitle("Range");
        buildPane(aircraftFramePane).setTitle("Frame");

        VBox vBox = new VBox(aircraftAirToAirPane, aircraftPerformancePane, aircraftFramePane);
        vBox.getStyleClass().add("components-pane");

        return vBox;
    }

    /**
     * Build a component pane.
     *
     * @param pane The pane to build.
     * @return The built pane.
     */
    private TitledGridPane buildPane(final TitledGridPane pane) {
        return pane.setWidth(props.getInt("ship.dialog.detailsPane.width"))
                .setStyleId("component-grid")
                .buildPane();
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
        details.put("Service:", aircraft.getService().toString());
        return details;
    }

    /**
     * Get the squadron's attack factor data.
     *
     * @param factor The attack factor.
     * @param prob The probability of success.
     * @return The aircraft's attack data.
     */
    private Map<String, List<String>> getAttackFactor(final SquadronFactor factor, final double prob) {
        Map<String, List<String>> details = new LinkedHashMap<>();
        String defensive = factor.isDefensive() ? " (D)" : "";
        details.put("Factor:", Arrays.asList(factor.getFactor() + defensive, "Hit:", probability.percentage(prob) + "%"));
        details.put("Modifier:", Collections.singletonList(factor.getModifier() + ""));
        return details;
    }

    /**
     * Get the squadron's ferryDistance data.
     *
     * @param squadron The selected squadron.
     * @param config The selected squadron's configuration.
     * @return The squadron's ferryDistance data.
     */
    private Map<String, List<String>> getPerformance(final Squadron squadron, final SquadronConfig config) {
        Aircraft aircraft = squadron.getAircraft();
        Map<String, List<String>> details = new LinkedHashMap<>();
        details.put("Range:", Arrays.asList(aircraft.getRange() + "", "Radius:", aircraft.getRadius().get(config) + ""));
        details.put("Endurance:", Arrays.asList(aircraft.getEndurance().get(config) + "", "Ferry:", aircraft.getFerryDistance().get(config) + ""));
        details.put("Altitude Rating:", Collections.singletonList(aircraft.getAltitude().toString()));
        details.put("Landing Type:", Collections.singletonList(aircraft.getLanding().toString()));
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
        details.put("Frame:", aircraft.getFrame().getFrame() + "");
        details.put("Fragile:", aircraft.getFrame().isFragile() + "");
        return details;
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

    /**
     * Get the aircraft's profile image.
     *
     * @param squadron The selected squadron.
     * @return The aircraft's profile image view.
     */
    private Image getProfile(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        return imageResourceProvider.getAircraftProfileImageView(squadron.getSide(), aircraft.getModel());
    }
}
