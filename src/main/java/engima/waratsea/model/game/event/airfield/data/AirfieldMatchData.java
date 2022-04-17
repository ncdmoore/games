package engima.waratsea.model.game.event.airfield.data;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import lombok.Data;

/**
 * Represents the data used to match airfield events. This class is read from a JSON file.
 */
@Data
public class AirfieldMatchData {
    private String action;     // The action that the airfield experienced.
    private String name;       // The airfield that experienced the event.
    private Side side;
    private int value;
    private AssetType by;      // The game asset ship, sub or aircraft that caused the event. The asset that did the event. Not all events have a by.
}
