package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirfieldSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private Airfield airfield;

    /**
     * Constructor called by guice.
     *
     * @param imageResourceProvider Provides images.
     * @param props Provides view properties.
     */
    @Inject
    public AirfieldSummaryView(final ImageResourceProvider imageResourceProvider,
                               final ViewProps props) {
        this.imageResourceProvider = imageResourceProvider;
        this.props = props;
    }

    /**
     * Set the airfield.
     *
     * @param field The airfield.
     * @return The airfield patrol view.
     */
    public AirfieldSummaryView setAirfield(final Airfield field) {
        this.airfield = field;
        return this;
    }

    /**
     * Show the airfield summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The node containing the airfield summary.
     */
    public Node show(final Nation nation) {
        AirfieldType airfieldType = airfield.getAirfieldType();

        ImageView airfieldView = imageResourceProvider.getImageView(nation + "Airfield" + airfieldType + "Details.png");

        TitledPane airfieldTitle = buildAirfieldTitle();
        TitledGridPane airfieldDetails = buildAirfieldDetails(nation);
        TitledGridPane airfieldSteps = buildAirfieldSteps(nation);
        TitledPane landingTypes = buildLandingTypes();

        VBox leftVBox = new VBox(airfieldTitle, airfieldView, airfieldDetails, airfieldSteps, landingTypes);
        leftVBox.setId("airfield-summary-vbox");

        return leftVBox;
    }

    /**
     * Build the airfield title pane.
     *
     * @return A title pane with the airfield's title.
     */
    private TitledPane buildAirfieldTitle() {
        TitledPane titledPane = new TitledPane();
        titledPane.setText(airfield.getTitle());
        titledPane.setId("airfield-title-pane");
        return titledPane;
    }

    /**
     * Build the airfield details.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield details.
     */
    private TitledGridPane buildAirfieldDetails(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .setTitle("Airfield Stats")
                .buildPane(getAirfieldDetails(nation));
    }

    /**
     * Get the airfield details.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of the airfield details.
     */
    private Map<String, String> getAirfieldDetails(final Nation nation) {
        Map<String, String> details = new LinkedHashMap<>();

        if (airfield.getRegion(nation).getMax() == 0) {
            details.put("Max Region Capacity:", "-");
        } else {
            details.put("Max Region Capacity:", airfield.getRegion(nation).getMax() + "");
        }

        if (airfield.getRegion(nation).getMin() == 0) {
            details.put("Min Region Capacity:", "-");
        } else {
            details.put("Min Region Capacity:", airfield.getRegion(nation).getMin() + "");
        }

        details.put("Max Capacity:", airfield.getMaxCapacity() + "");
        details.put("Current Capacity:", airfield.getCapacity() + "");
        details.put("Current deployed:", airfield.getCurrentSteps() + "");
        details.put("AA Rating:", airfield.getAntiAir() + "");

        return details;
    }

    /**
     * Build the airfield squadron step summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield step summary.
     */
    private TitledGridPane buildAirfieldSteps(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .setTitle("Airfield Step Summary")
                .buildPane(getAirfieldSteps(nation));
    }

    /**
     * Build landing types for the airfield.
     *
     * @return A titled pane containing the landing types.
     */
    private TitledPane buildLandingTypes() {
        TitledPane titledPane = new TitledPane();

        titledPane.setText("Supported Landing Types");

        List<CheckBox> checkBoxes = Stream.of(LandingType.values())
                .filter(landingType -> landingType != LandingType.CARRIER)
                .map(this::buildCheckBox)
                .collect(Collectors.toList());

        VBox vBox = new VBox();
        vBox.getChildren().addAll(checkBoxes);

        titledPane.setContent(vBox);
        vBox.setId("landing-type-pane");

        return titledPane;
    }

    /**
     * Build the landing type check boxes that indicate which
     * landing types the airfield support.
     *
     * @param landingType An aircraft/squadron landing type.
     * @return A checkbox corresponding to the given landing type.
     */
    private CheckBox buildCheckBox(final LandingType landingType) {
        CheckBox checkBox = new CheckBox(landingType.toString());
        if (airfield.getLandingType().contains(landingType)) {
            checkBox.setSelected(true);
        }
        checkBox.setDisable(true);
        return checkBox;
    }

    /**
     * Get the airfield step summary for each type of squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of the airfield steps where the key is the type
     * of squadron and the value is the total number of steps of that
     * type of squadron.
     */
    private Map<String, String> getAirfieldSteps(final Nation nation) {
        Map<SquadronViewType, BigDecimal> steps = airfield.getStepMap(nation)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> SquadronViewType.get(e.getKey()),
                        Map.Entry::getValue,
                        BigDecimal::add,
                        LinkedHashMap::new));

        // Convert the map to a string key value pair for display on the GUI.
        return steps
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey() + ":",
                        e -> e.getValue().toString(),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }
}
