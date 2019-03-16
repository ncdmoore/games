package engima.waratsea.model.aircraft.data;

import lombok.Getter;
import lombok.Setter;

/**
 * An aircraft's range data.
 */
public class RangeData {
    @Getter
    @Setter
    private int range;

    @Getter
    @Setter
    private int endurance;
}
