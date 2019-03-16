package engima.waratsea.model.squadron.allotment.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a given aircraft type's number of squadrons.
 */
public class AllotmentAircraftData {
    @Getter
    @Setter
    private String type;  // The type of aircraft.

    @Getter
    @Setter
    private int number; // The maximum number of squadrons of this type of aircraft.
}
