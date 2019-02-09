package engima.waratsea.model.ships;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.TaskForce;

/**
 * Represents a ship.
 */
public interface Ship {


    /**
     * Get the ship's id.
     * @return The ship's id.
     */
    ShipId getShipId();

    /**
     * Get the ship's name.
     * @return The ship's name.
     */
    String getName();

    /**
     * Get the ship's task force.
     * @return The ship's task force.
     */
    TaskForce getTaskForce();

    /**
     * Get the ship's type. Aircraft carrier, battleship, battlecruiser, etc.
     * @return The ship's type.
     */
    ShipType getType();

    /**
     * Determines if this ship is an aircraft carrier.
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    boolean isCarrier();

    /**
     * Get the ship's nationality.
     * @return The ship's nationality.
     */
    Nation getNationality();

    /**
     * Get the ship's victory points if sunk.
     * @return The ship's victory points.
     */
    int getVictoryPoints();

    /**
     * Set the ship's task force.
     * @param taskForce The ship's task force.
     */
    void setTaskForce(TaskForce taskForce);

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    void loadCargo();

    /**
     * Get the ship's cargo data.
     * @return The ship's cargo data.
     */
    Cargo getCargo();
}
