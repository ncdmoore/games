package engima.waratsea.view.squadron;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AttackFactor;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.ImageResourceProvider;
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

import java.util.LinkedHashMap;
import java.util.Map;

public class SquadronDetailsView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private ImageView aircraftImage = new ImageView();
    private ImageView aircraftProfile = new ImageView();
    private TitledGridPane squadronDetailsPane;
    private TitledGridPane aircraftDetailsPane;
    private TitledGridPane aircraftAirToAirPane;
    private TitledGridPane aircraftLandPane;
    private TitledGridPane aircraftNavalPane;
    private TitledGridPane aircraftRangePane;
    private TitledGridPane aircraftFramePane;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props View properties.
     */
    @Inject
    public SquadronDetailsView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Show the squadron details.
     *
     * @param squadron The squadron whose details are shown.
     * @return A node containing the squadron details.
     */
    public Node show(final Squadron squadron) {
        Label title = new Label(squadron.getName());
        title.setId("title");

        StackPane titlePane = new StackPane(title);
        titlePane.setId("title-pane-" + squadron.getSide().getPossesive().toLowerCase());

        Node imageBox = buildImage(squadron);
        imageBox.setId("squadron-image");

        VBox listBox = new VBox(imageBox);
        listBox.setId("details-pane");

        Node weaponComponentsVBox = buildWeapons(squadron);
        weaponComponentsVBox.setId("components-pane");

        VBox performanceComponetsVBox = buildPerformance(squadron);

        Node profileBox = buildProfile(squadron);
        listBox.getChildren().add(profileBox);

        performanceComponetsVBox.setId("components-pane");

        HBox hBox = new HBox(listBox, weaponComponentsVBox, performanceComponetsVBox);
        hBox.setId("main-hbox");

        VBox mainPane = new VBox(titlePane, hBox);
        mainPane.setId("main-pane");

        return mainPane;
    }

    /**
     * Build the squadron aircraft image.
     *
     * @param squadron The squadron.
     * @return The node that contains the squadron image.
     */
    public Node buildImage(final Squadron squadron) {
        aircraftImage.setImage(getImage(squadron));
        return new VBox(aircraftImage);
    }

    /**
     * Build the aircraft profile image.
     *
     * @param squadron The squadron whose aircraft profile image is built.
     * @return The node containing the aircraft profile image.
     */
    public Node buildProfile(final Squadron squadron) {
        TitledPane titledPane = new TitledPane();
        titledPane.setText("Profile");
        aircraftProfile.setImage(getProfile(squadron));

        VBox vBox = new VBox(aircraftProfile);
        vBox.setId("profile-vbox");

        titledPane.setContent(vBox);
        titledPane.setMinHeight(props.getInt("ship.dialog.profile.height"));
        titledPane.setMaxHeight(props.getInt("ship.dialog.profile.height"));
        return titledPane;
    }

    /**
     * Build the squadron weapon components.
     *
     * @param squadron The squadron.
     * @return The node that contains the squadron components.
     */
    public Node buildWeapons(final Squadron squadron) {
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
    public VBox buildPerformance(final Squadron squadron) {
        aircraftAirToAirPane = buildPane("Air-to-Air", getAttackFactor(squadron, squadron.getAircraft().getAir()));
        aircraftRangePane = buildPane("Performance", getRange(squadron));
        aircraftFramePane = buildPane("Frame", getFrame(squadron));
        return new VBox(aircraftAirToAirPane, aircraftRangePane, aircraftFramePane);
    }

    /**
     * Select one of the ship's squadrons.
     *
     * @param squadron The selected squadron.
     */
    public void selectSquadron(final Squadron squadron) {
        aircraftImage.setImage(getImage(squadron));
        aircraftProfile.setImage(getProfile(squadron));
        squadronDetailsPane.updatePane(getSquadronDetailsData(squadron));
        aircraftDetailsPane.updatePane(getAircraftDetailsData(squadron));
        aircraftAirToAirPane.updatePane(getAttackFactor(squadron, squadron.getAircraft().getAir()));
        aircraftLandPane.updatePane(getAttackFactor(squadron, squadron.getAircraft().getLand()));
        aircraftNavalPane.updatePane(getAttackFactor(squadron, squadron.getAircraft().getNaval()));
        aircraftRangePane.updatePane(getRange(squadron));
        aircraftFramePane.updatePane(getFrame(squadron));
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
        details.put("Service", aircraft.getService().toString());
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
     * Get the squadron's ferryDistance data.
     *
     * @param squadron The selected squadron.
     * @return The squadron's ferryDistance data.
     */
    private Map<String, String> getRange(final Squadron squadron) {
        Aircraft aircraft = squadron.getAircraft();
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Range", aircraft.getRange().getFerryDistance() + "");
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
                .buildPane(title, data);
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
