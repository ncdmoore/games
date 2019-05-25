package engima.waratsea.model.game.event.airfield.data;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the data used to match airfield events. This class is read from a JSON file.
 */
public class AirfieldMatchData {
    @Getter
    @Setter
    private String action;     // The action that the airfield experienced.

    @Getter
    @Setter
    private String name;       // The airfield that experienced the event.

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private int value;

    @Getter
    @Setter
    private AssetType by;          // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.
}
