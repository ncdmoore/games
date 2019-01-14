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
    private Cargo cargo;

    /**
     * Constructor called by guice.
     * @param data Ship's data.
     */
    @Inject
    public SurfaceShip(@Assisted final ShipData data) {
        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nationality = data.getNationality();

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
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return false;
    }

    /**
     * Get the ship's victory points if sunk.
     *
     * @return The ship's victory points.
     */
    @Override
    public int getVictoryPoints() {
        return 0;
    }

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    @Override
    public void loadCargo() {
        cargo.load();
    }
}
