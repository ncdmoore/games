package engima.waratsea.model.aircraft.data;

import lombok.Getter;
import lombok.Setter;

/**
 * An aircraft's ferryDistance data.
 */
public class PerformanceData {
    @Getter @Setter private int range;
    @Getter @Setter private int endurance;
}
