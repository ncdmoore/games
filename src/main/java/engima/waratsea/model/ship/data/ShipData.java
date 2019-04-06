package engima.waratsea.model.ship.data;

import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the ship's data that is read and written to JSON files.
 */
public class ShipData {
    @Getter
    @Setter
    private transient ShipId shipId;

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
    private FlightDeckData flightDeck;

    @Getter
    @Setter
    private GunData primary;

    @Getter
    @Setter
    private GunData secondary;

    @Getter
    @Setter
    private GunData tertiary;

    @Getter
    @Setter
    private GunData antiAir;

    @Getter
    @Setter
    private TorpedoData torpedo;

    @Getter
    @Setter
    private HullData hull;

    @Getter
    @Setter
    private FuelData fuel;

    @Getter
    @Setter
    private MovementData movement;

    @Getter
    @Setter
    private CargoData cargo;                                            //Cargo is 3 times the value in the board game. This is done to avoid fractions.

    @Getter
    @Setter
    private int victoryPoints;

    @Getter
    @Setter
    private String originPort;

    @Getter
    @Setter
    private List<AircraftData> aircraft;

    /** The default ship data constructor.
     *
     */
    public ShipData() {
        primary = new GunData();
        secondary = new GunData();
        tertiary = new GunData();
        antiAir = new GunData();
        torpedo = new TorpedoData();
        cargo = new CargoData();
    }
}
