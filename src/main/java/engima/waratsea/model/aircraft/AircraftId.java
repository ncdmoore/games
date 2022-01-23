package engima.waratsea.model.aircraft;

import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Since a given aircraft may at one time be on the Allied side and then later be on the Axis side, to uniquely
 * identify an aircraft requires both the aircraft type and the side. This class is used to uniquely identify aircraft.
 */
@RequiredArgsConstructor
public class AircraftId {
    @Getter private final String model;
    @Getter private final Side side;
}
