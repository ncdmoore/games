package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.TitledGridPane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirfieldPatrolInfo {
    private final ViewProps props;

    private Nation nation;
    private Airbase airbase;

    private final TitledGridPane patrolCountsPane = new TitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldPatrolInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public TitledGridPane build() {
        return buildPatrolCounts();
    }

    /**
     * Set the airbase.
     *
     * @param selectedNation The selected nation.
     * @param selectedAirbase  The selected airbase.
     */
    public void setAirbase(final Nation selectedNation, final Airbase selectedAirbase) {
        nation = selectedNation;
        airbase = selectedAirbase;
        patrolCountsPane.buildPane(getAirfieldPatrolSummary());
    }

    /**
     * Update the given patrol types value.
     *
     * @param key The patrol type.
     * @param value The number of squadrons on the given patrol type.
     */
    public void update(final PatrolType key, final int value) {
        patrolCountsPane.updateGrid(key.getValue() + ":", value + "");
    }

    /**
     * Update the patrol counts pane.
     */
    public void update() {
        patrolCountsPane.buildPane(getAirfieldPatrolSummary());
    }

    /**
     * Build the airfield squadron summary.
     *
     * @return A titled grid pane containing the airfield squadron summary.
     */
    private TitledGridPane buildPatrolCounts() {
        return buildPane().setTitle("Patrol Summary");
    }

    /**
     * Build a component pane.
     *
     * @return The built pane.
     */
    private TitledGridPane buildPane() {
        return patrolCountsPane.setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .buildPane();
    }

    /**
     * Get the airfield squadron patrol summary.
     *
     * @return A map of patrol type to squadrons on the given patrol.
     */
    private Map<String, String> getAirfieldPatrolSummary() {
        return Stream.of(PatrolType.values()).sorted().collect(Collectors.toMap(
                patrolType -> patrolType.getValue() + ":",
                patrolType -> airbase
                        .getPatrol(patrolType)
                        .getSquadrons(nation)
                        .size() + "",
                (oldValue, newValue) -> oldValue,
                LinkedHashMap::new));
    }
}
