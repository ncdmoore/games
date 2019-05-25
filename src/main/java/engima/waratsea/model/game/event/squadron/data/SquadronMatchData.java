package engima.waratsea.model.game.event.squadron.data;

import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the data used to match squadron events. This class is read from a JSON file.
 */
public class SquadronMatchData {
    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private String aircraftModel;

    @Getter
    @Setter
    private String aircraftType;

    @Getter
    @Setter
    private String startingLocation;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private AssetType by;    // The game asset Ship, aircraft or sub that caused the event to fire. The asset that did the event.
}
