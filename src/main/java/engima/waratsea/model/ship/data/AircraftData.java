package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Ship aircraft data.
 */
public class AircraftData {
    @Getter
    @Setter
    private String model;

    @Getter
    @Setter
    private int steps;
}
