package engima.waratsea.model.submarine.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.data.FuelData;
import engima.waratsea.model.ship.data.MovementData;
import engima.waratsea.model.ship.data.TorpedoData;
import lombok.Data;

/**
 * Represents submarine data read in from a JSON file.
 */
@Data
public class SubmarineData {
    private ShipId shipId;
    private ShipType type;
    private String shipClass;
    private Nation nationality;
    private TorpedoData torpedo;
    private MovementData movement;
    private FuelData fuel;
    private int victoryPoints;
}
