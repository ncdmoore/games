package engima.waratsea.model.aircraft.data;

import lombok.Getter;
import lombok.Setter;

/**
 * An aircraft's ferryDistance data.
 */
public class RangeData {
    @Getter
    @Setter
    private int ferryDistance;

    @Getter
    @Setter
    private int endurance;
}
