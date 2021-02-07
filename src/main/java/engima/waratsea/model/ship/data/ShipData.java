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
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the ship's data that is read and written to JSON files.
 */
public class ShipData {
    @Getter @Setter private transient ShipId shipId;
    @Getter @Setter private transient TaskForce taskForce;
    @Getter @Setter private ShipType type;
    @Getter @Setter private String shipClass;
    @Getter @Setter private Nation nationality;
    @Getter @Setter private FlightDeckData flightDeck;
    @Getter @Setter private GunData primary;
    @Getter @Setter private GunData secondary;
    @Getter @Setter private GunData tertiary;
    @Getter @Setter private GunData antiAir;
    @Getter @Setter private AmmunitionType ammunitionType;
    @Getter @Setter private TorpedoData torpedo;
    @Getter @Setter private AswData asw;
    @Getter @Setter private HullData hull;
    @Getter @Setter private FuelData fuel;
    @Getter @Setter private MovementData movement;
    @Getter @Setter private CargoData cargo;             //Cargo is 3 times the value in the board game. This is done to avoid fractions.
    @Getter @Setter private int victoryPoints;
    @Getter @Setter private String originPort;
    @Getter @Setter private List<LandingType> landingType;

    @Getter @Setter private SquadronsData squadronsData; //squadrons stationed at the airfield.
    @Getter @Setter private MissionsData missionsData;
    @Getter @Setter private PatrolsData patrolsData;


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
