package engima.waratsea.model.game.event.airfield.data;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.event.airfield.AirfieldEventAction;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the data used to match airfield events. This class is read from a JSON file.
 */
public class AirfieldMatchData {
    @Getter
    @Setter
    private Airfield airfield;              // The airfield that experienced the event.

    @Getter
    @Setter
    private AirfieldEventAction action;     // The action that the airfield experienced.

    @Getter
    @Setter
    private int data;

    @Getter
    @Setter
    private Asset by;                       // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.
}
