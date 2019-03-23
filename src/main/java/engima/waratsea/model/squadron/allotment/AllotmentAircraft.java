package engima.waratsea.model.squadron.allotment;

import engima.waratsea.model.squadron.allotment.data.AllotmentAircraftData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an allotment of a given type of aircraft.
 */
@Slf4j
public class AllotmentAircraft {
    private String type;  // The type of aircraft. BF109E for example.
    private int number; // The number of squadrons of this type of aircraft.

    /**
     * The constructor.
     *
     * @param data The allotment aircraft data read in from a JSON file.
     */
    public AllotmentAircraft(final AllotmentAircraftData data) {
        type = data.getType();
        number = data.getNumber();
    }

    /**
     * Returns a list of the aircraft type. The list will contain 'number' of type entries.
     * Basically, a list of {"type", "type", "type, ...}
     *
     * @return A list of aircraft type names.
     */
    public List<String> get() {
        List<String> squadrons = new ArrayList<>();

        for (int i = 0; i < number; i++) {
            squadrons.add(type);
        }

        log.debug("Allotment for '{}' is '{}'", type, number);

        return squadrons;
    }
}
