package engima.waratsea.model.ship.data;

import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.mission.data.MissionsData;
import engima.waratsea.model.base.airfield.patrol.data.PatrolsData;
import engima.waratsea.model.base.airfield.squadron.data.SquadronsData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.AmmunitionType;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Data;

import java.util.List;

/**
 * Represents the ship's data that is read and written to JSON files.
 */
@Data
public class ShipData {
    private transient ShipId shipId;
    private transient TaskForce taskForce;
    private ShipType type;
    private String shipClass;
    private Nation nationality;
    private FlightDeckData flightDeck;
    private GunData primary;
    private GunData secondary;
    private GunData tertiary;
    private GunData antiAir;
    private AmmunitionType ammunitionType;
    private TorpedoData torpedo;
    private AswData asw;
    private HullData hull;
    private FuelData fuel;
    private MovementData movement;
    private CargoData cargo;             //Cargo is 3 times the value in the board game. This is done to avoid fractions.
    private int victoryPoints;
    private String originPort;
    private List<LandingType> landingType;

    private SquadronsData squadronsData; //squadrons stationed at the airfield.
    private MissionsData missionsData;
    private PatrolsData patrolsData;


    /** The default ship data constructor.
     *
     */
    public ShipData() {
        primary = new GunData();
        secondary = new GunData();
        tertiary = new GunData();
        antiAir = new GunData();
        torpedo = new TorpedoData();
        asw = new AswData();
        cargo = new CargoData();
    }
}
