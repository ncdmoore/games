package engima.waratsea.model.ships;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ships.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a surface ship: Battleship, cruisers, etc.
 */
public class SurfaceShip implements Ship {

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

    private Gun primary;
    private Gun secondary;
    private Gun tertiary;
    private Gun antiAir;
    private Torpedo torpedo;
    private Movement movement;
    private Fuel fuel;
    private Hull hull;

    @Getter
    private Cargo cargo;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     */
    @Inject
    public SurfaceShip(@Assisted final ShipData data) {
        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nationality = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        primary = new Gun(data.getWeapons().getPrimary(), data.getArmour().getPrimary());
        secondary = new Gun(data.getWeapons().getSecondary(), data.getArmour().getSecondary());
        tertiary = new Gun(data.getWeapons().getTertiary(), data.getArmour().getTertiary());
        antiAir = new Gun(data.getWeapons().getAa(), data.getArmour().getAa());
        torpedo = new Torpedo(data.getWeapons().getTorpedo());

        movement = new Movement(data.getMovement().getEven(), data.getMovement().getOdd());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull(), data.getArmour().getHull());
        cargo = new Cargo((data.getCargo()));
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
    public String getOriginPort() {
        return cargo.getOriginPort();
    }

    /**
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return false;
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
