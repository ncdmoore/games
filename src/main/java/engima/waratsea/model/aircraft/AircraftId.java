package engima.waratsea.model.aircraft;

import engima.waratsea.model.game.Side;
import lombok.Data;

/**
 * Since a given aircraft may at one time be on the Allied side and then later be on the Axis side, to uniquely
 * identify an aircraft requires both the aircraft type and the side. This class is used to uniquely identify aircraft.
 */
@Data
public class AircraftId {
    private final String model;
    private final Side side;
}
