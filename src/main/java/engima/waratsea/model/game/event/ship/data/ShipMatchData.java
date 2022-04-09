package engima.waratsea.model.game.event.ship.data;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import lombok.Data;

/**
 * Represents the data used to match ship events. This class is read from a JSON file.
 */
@Data
public class ShipMatchData {
    private String action;
    private String name;
    private Side side;
    private Nation nation;
    private String taskForceName;
    private String shipType;
    private String location;
    private String startingLocation;
    private AssetType by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.
}
