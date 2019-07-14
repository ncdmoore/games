package engima.waratsea.model.motorTorpedoBoat.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.data.FuelData;
import engima.waratsea.model.ship.data.MovementData;
import engima.waratsea.model.ship.data.TorpedoData;
import lombok.Getter;
import lombok.Setter;

public class MotorTorpedoBoatData {
    @Getter
    @Setter
    private ShipId shipId;

    @Getter
    @Setter
    private ShipType type;

    @Getter
    @Setter
    private String shipClass;

    @Getter
    @Setter
    private Nation nationality;

    @Getter
    @Setter
    private TorpedoData torpedo;

    @Getter
    @Setter
    private MovementData movement;

    @Getter
    @Setter
    private FuelData fuel;

    @Getter
    @Setter
    private int victoryPoints;
}
