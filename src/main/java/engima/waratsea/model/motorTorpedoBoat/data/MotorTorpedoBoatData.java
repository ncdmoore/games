package engima.waratsea.model.motorTorpedoBoat.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.data.FuelData;
import engima.waratsea.model.ship.data.MovementData;
import engima.waratsea.model.ship.data.TorpedoData;
import lombok.Data;

@Data
public class MotorTorpedoBoatData {
    private ShipId shipId;
    private ShipType type;
    private String shipClass;
    private Nation nationality;
    private TorpedoData torpedo;
    private MovementData movement;
    private FuelData fuel;
    private int victoryPoints;
}
