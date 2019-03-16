package engima.waratsea.model.aircraft;

import engima.waratsea.model.game.Side;
import lombok.Getter;

/**
 * Since a given aircraft may at one time be on the Allied side and then later be on the Axis side, to uniquely
 * identify an aircraft requires both the aircraft type and the side. This class is used to uniquely identify aircraft.
 */
public class AircraftId {
    @Getter
    private final String model;

    @Getter
    private final Side side;

    /**
     * Constructor.
     *
     * @param model The model of aircraft.
     * @param side The side the aircraft is on: ALLIED or AXIS.
     */
    public AircraftId(final String model, final Side side) {
        this.model = model;
        this.side = side;
    }
}
