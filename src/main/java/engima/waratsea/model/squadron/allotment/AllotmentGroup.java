package engima.waratsea.model.squadron.allotment;

import engima.waratsea.model.squadron.allotment.data.AllotmentGroupData;
import engima.waratsea.model.squadron.data.SquadronData;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * The allotment data for a group of aircraft.
 */
@Slf4j
public class AllotmentGroup {
    @Getter
    @Setter
    private int selectSize;

    private final List<SquadronData> aircraft;    // A list of "all" available squadron types for this grouping.

    /**
     * Constructor.
     *
     * @param data The allotment group data read in from a JSON file.
     */
    public AllotmentGroup(final AllotmentGroupData data) {
        selectSize = data.getSelectSize();

        // This list contains a list of aircraft type names. Each name may be repeated.
        // For example:
        //
        //     {"Blenheim", "Blenheim", ... "Wellington", "Wellington", "Wellington", ... }
        //
        aircraft = data.getAircraft()
                .stream()
                .map(AllotmentAircraft::new)
                .flatMap(allotment -> allotment.get().stream())
                .collect(Collectors.toList());

        log.debug("The select size for is: '{}', aircraft: '{}'", selectSize, aircraft
                .stream()
                .map(SquadronData::getModel)
                .distinct()
                .collect(Collectors.joining(", ")));
    }

    /**
     * Select the selectSize of squadrons from this group.
     *
     * @param numberNeeded The number of squadrons to select from this group. This may be 0.
     * @return A list of selected squadron aircraft types.
     */
    public List<SquadronData> select(final int numberNeeded) {

        // If the number needed is less that the select size, then only select what is needed.
        int numberToSelect = numberNeeded < selectSize ? numberNeeded : selectSize;


        if (numberToSelect > aircraft.size()) {
            log.error("Not enough aircraft available for selection.");
            numberToSelect = aircraft.size();
        }

        List<SquadronData> selected = new ArrayList<>();

        for (int i = 0; i < numberToSelect; i++) {
            int index = new Random().nextInt(aircraft.size());         // Get an index to remove from the squadron list.
            SquadronData squadron = aircraft.remove(index);            // The selected squadron.
            selected.add(squadron);                                    // Add the selected squadron to the selected list.
        }

        return selected;
    }
}
