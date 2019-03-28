package engima.waratsea.model.ship;

import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;

import java.util.Map;

/**
 * Represents a ship.
 */
public interface Ship extends PersistentData<ShipData> {

    /**
     * Get the ship's id.
     *
     * @return The ship's id.
     */
    ShipId getShipId();

    /**
     * Get the ship's side: ALLIES or AXIS.
     *
     * @return The ship's side.
     */
    Side getSide();

    /**
     * Get the ship's name.
     *
     * @return The ship's name.
     */
    String getName();

    /**
     * Get the ship's origin port. This is tracked at the ship level because a ship may be moved from one task
     * force to another. Thus, the ship must keep track of its origin port itself.
     *
     * @return The port the ship sailed from.
     */
    String getOriginPort();

    /**
     * Get the ship's task force.
     *
     * @return The ship's task force.
     */
    TaskForce getTaskForce();

    /**
     * Get the ship's type. Aircraft carrier, battleship, battlecruiser, etc.
     *
     * @return The ship's type.
     */
    ShipType getType();

    /**
     * Get the ship's class. Not the java class, but the class of ship.
     *
     * @return The ship's class.
     */
    String getShipClass();

    /**
     * Determines if this ship is an aircraft carrier.
     *
     * @return True if this ship is an aircraft carrier. False otherwise.
     */
    boolean isCarrier();

    /**
     * Get the ship's nationality.
     *
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
     *
     * @param taskForce The ship's task force.
     */
    void setTaskForce(TaskForce taskForce);

    /**
     * Call this method to inform the ship that it is sailing from port.
     */
    void setSail();

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    void loadCargo();

    /**
     * Get the ship's cargo data.
     *
     * @return The ship's cargo data.
     */
    Cargo getCargo();

    /**
     * Get the ship's surface weapon data.
     *
     * @return A map of surface weapon data.
     */
    Map<String, String> getSurfaceWeaponData();

    /**
     * Get the ship's anti air weapon data.
     *
     * @return A map of the anti air weapon data.
     */
    Map<String, String> getAntiAirWeaponData();

    /**
     * Get the ship's torpedo data.
     *
     * @return The ship's torpedo data.
     */
    Map<String, String> getTorpedoData();

    /**
     * Get the ship's armour data.
     *
     * @return A map of the armour type to armour value.
     */
    Map<String, String> getArmourData();

    /**
     * Get the ship's movement data.
     *
     * @return A map of the movement per turn type.
     */
    Map<String, String> getMovementData();

    /**
     * Get the ship's persistent data.
     *
     * @return The ship's persistent data.
     */
    ShipData getData();
}
