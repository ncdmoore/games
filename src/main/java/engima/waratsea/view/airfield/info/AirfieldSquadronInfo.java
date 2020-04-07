package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.view.util.TitledGridPane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AirfieldSquadronInfo {
    private final ViewProps props;

    private Nation nation;
    private Airbase airbase;

    private final TitledGridPane squadronCountsPane = new TitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldSquadronInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public TitledGridPane build() {
        return buildSquadronCounts();
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
        squadronCountsPane.updatePane(getSquadronCounts());
    }

    /**
     * Build the airfield squadron summary.
     *
     * @return A titled grid pane containing the airfield squadron summary.
     */
    private TitledGridPane buildSquadronCounts() {
        return buildPane().setTitle("Squadron Summary");
    }

    /**
     * Build a component pane.
     *
     * @return The built pane.
     */
    private TitledGridPane buildPane() {
        return squadronCountsPane.setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .buildPane();
    }

    /**
     * Get the airfield's squadron counts for each type of aircraft.
     *
     * @return A map of the airfield squadrons where the key is the type
     * of squadron and the value is the total number of squadrons of that
     * type of squadron.
     */
    private Map<String, String> getSquadronCounts() {
        Map<SquadronViewType, Integer> numMap = SquadronViewType
                .convertList(airbase.getSquadronMap(nation))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));

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
}
