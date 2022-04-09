package engima.waratsea.model.game.event.squadron.data;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import lombok.Data;

/**
 * Represents the data used to match squadron events. This class is read from a JSON file.
 */
@Data
public class SquadronMatchData {
    private String action;
    private String name;
    private Side side;
    private String aircraftModel;
    private String aircraftType;
    private String startingLocation;
    private String location;
    private AssetType by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.
}
