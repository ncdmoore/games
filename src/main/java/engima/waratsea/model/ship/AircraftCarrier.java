package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.Airbase;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.map.Location;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents an aircraft carrier.
 */
public class AircraftCarrier implements Ship, Airbase {

    @Getter
    private final ShipId shipId;

    @Getter
    private final ShipType type;

    @Getter
    private final String shipClass;

    @Getter
    private final Nation nationality;

    @Getter
    private final int victoryPoints;

    @Getter
    @Setter
    private TaskForce taskForce;

    private Gun secondary;
    private Gun tertiary;
    private Gun antiAir;
    private Torpedo torpedo;
    private Movement movement;
    private Fuel fuel;
    private Hull hull;


    @Getter
    private Cargo cargo;

    private FlightDeck flightDeck;
    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     */
    @Inject
    public AircraftCarrier(@Assisted final ShipData data) {
        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nationality = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        secondary = new Gun(data.getSecondary());
        tertiary = new Gun(data.getTertiary());
        antiAir = new Gun(data.getAntiAir());
        torpedo = new Torpedo(data.getTorpedo());

        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull());
        cargo = new Cargo(data.getCargo());

        flightDeck = new FlightDeck(data.getFlightDeck());
    }

    /**
     * Get the ship's persistent data.
     *
     * @return The ship's persistent data.
     */
    @Override
    public ShipData getData() {
        ShipData data = new ShipData();
        data.setShipId(shipId);
        data.setType(type);
        data.setShipClass(shipClass);
        data.setNationality(nationality);
        data.setVictoryPoints(victoryPoints);

        data.setSecondary(secondary.getData());
        data.setTertiary(tertiary.getData());
        data.setAntiAir(antiAir.getData());
        data.setTorpedo(torpedo.getData());

        data.setMovement(movement.getData());
        data.setFuel(fuel.getData());
        data.setHull(hull.getData());
        data.setCargo(cargo.getData());

        data.setFlightDeck(flightDeck.getData());
        return data;
    }

    /**
     * Get The aircraft carrier's current aircraft capacity.
     *
     * @return The current aircraft capacity in steps.
     */
    @Override
    public int getCapacity() {
        return flightDeck.getCapacity();
    }

    /**
     * Get the ship's name.
     *
     * @return The ship's name.
     */
    @Override
    public String getName() {
        return shipId.getName();
    }

    /**
     * Get the ship's origin port.
     *
     * @return The port the ship sailed from.
     */
    @Override
    public Location getOriginPort() {
        return cargo.getOriginPort();
    }

    /**
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return true;
    }

    /**
     * Call this method to inform the ship that it is sailing from port.
     */
    @Override
    public void setSail() {
        cargo.setOriginPort(taskForce.getLocation());
    }


    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    @Override
    public void loadCargo() {
        cargo.load(taskForce.getLocation());
    }
}
