package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the Airfield summary on the left side of the airfield details dialog.
 */
public class AirfieldSummaryView {
    private final ImageResourceProvider imageResourceProvider;
    private final ViewProps props;

    private Airfield airfield;

    private TitledGridPane airfieldPatrol;
    private TitledGridPane ready;

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
        TitledGridPane airfieldSummary = buildAirfieldSummary(nation);
        airfieldPatrol = buildAirfieldPatrol(nation);
        ready = buildReady(nation);
        TitledPane landingTypes = buildLandingTypes();

        Accordion accordion = new Accordion();
        accordion.getPanes().addAll(airfieldDetails, airfieldSummary, airfieldPatrol, ready);
        accordion.setExpandedPane(airfieldDetails);

        VBox leftVBox = new VBox(airfieldTitle, airfieldView, accordion, landingTypes);
        leftVBox.setId("airfield-summary-vbox");

        return leftVBox;
    }

    /**
     * Update the patrol summary.
     *
     * @param key The patrol type,
     * @param value The number of squadrons on the patrol.
     */
    public void updatePatrolSummary(final PatrolType key, final int value) {
        airfieldPatrol.updateGrid(key.getValue() + ":", value + "");
    }

    /**
     * Update the ready summary.
     *
     * @param key The squadron view type.
     * @param value The number of squadron ready.
     */
    public void updateReadySummary(final SquadronViewType key, final int value) {
        ready.updateGrid(key.getValue() + ":", value + "");
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
                .setTitle("Airfield Summary")
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
     * Build the airfield squadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield squadron summary.
     */
    private TitledGridPane buildAirfieldSummary(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .setTitle("Squadron Summary")
                .buildPane(getAirfieldSummary(nation));
    }

    /**
     * Build the airfield patrol summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield patrol summary.
     */
    private TitledGridPane buildAirfieldPatrol(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .setTitle("Patrol Summary")
                .buildPane(getAirfieldPatrolSummary(nation));
    }

    /**
     * Build the airfield ready summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A titled grid pane containing the airfield ready summary.
     */
    private TitledGridPane buildReady(final Nation nation) {
        return new TitledGridPane()
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setStyleId("component-grid")
                .setTitle("Ready Summary")
                .buildPane(getAirfieldReadySummary(nation));
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
     * Get the airfield squadron summary for each type of squadron.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of the airfield squadrons where the key is the type
     * of squadron and the value is the total number of squadrons of that
     * type of squadron.
     */
    private Map<String, String> getAirfieldSummary(final Nation nation) {
        Map<SquadronViewType, Integer> numMap = airfield.getSquadrons(nation)
                .stream()
                .collect(Collectors.toMap(squadron -> SquadronViewType.get(squadron.getType()),
                        squadron -> 1,
                        Integer::sum,
                        LinkedHashMap::new));

        // Add in zero's for the squadron types not present at this airfield.
        Stream.of(SquadronViewType.values()).forEach(type -> {
            if (!numMap.containsKey(type)) {
                numMap.put(type, 0);
            }
        });

        return Stream.of(SquadronViewType.values())
                .sorted()
                .collect(Collectors.toMap(type -> type.getValue() + ":",
                        type -> numMap.get(type).toString(),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }

    /**
     * Get the airfield squadron patrol summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of patrol type to squadrons on the given patrol.
     */
    private Map<String, String> getAirfieldPatrolSummary(final Nation nation) {
        return Stream.of(PatrolType.values()).sorted().collect(Collectors.toMap(
                patrolType -> patrolType.getValue() + ":",
                patrolType -> airfield
                        .getPatrol(patrolType)
                        .getSquadrons(nation)
                        .size() + "",
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new));
    }

    /**
     * Get the airfield ready squadron summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A map of squadron view types to number of ready squadrons of that type.
     */
    private Map<String, String> getAirfieldReadySummary(final Nation nation) {
         Map<SquadronViewType, Integer> readyMap = airfield.getSquadrons(nation, SquadronState.READY)
                .stream()
                .collect(Collectors.toMap(squadron -> SquadronViewType.get(squadron.getType()),
                                          squadron -> 1,
                                          Integer::sum,
                                          LinkedHashMap::new));

        // Add in zero's for the squadron types not present at this airfield.
        Stream.of(SquadronViewType.values()).forEach(type -> {
            if (!readyMap.containsKey(type)) {
                readyMap.put(type, 0);
            }
        });

        return Stream.of(SquadronViewType.values())
                .sorted()
                .collect(Collectors.toMap(type -> type.getValue() + ":",
                        type -> readyMap.get(type).toString(),
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
    }
}
