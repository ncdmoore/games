package engima.waratsea.model.ship;

import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.vessel.Vessel;

import java.util.List;

/**
 * Represents a ship.
 */
public interface Ship extends Vessel, PersistentData<ShipData> {
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
     * Get the ship's title. Some ships have revisions or configurations in their name.
     * The getName routine returns this extra information. The get title routine only
     * returns the ship's name/title.
     *
     * @return The ship's title.
     */
    String getTitle();

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
     * Determines if this ship is an capable of air operations. It is either an aircraft carrier or a ship with
     * float planes.
     *
     * @return True if this ship is an aircraft carrier or float plane capable. False otherwise.
     */
    boolean isAirbase();

    /**
     * Get the ship's nationality.
     *
     * @return The ship's nationality.
     */
    Nation getNation();

    /**
     * Get the ship's victory points if sunk.
     * @return The ship's victory points.
     */
    int getVictoryPoints();

    /**
     * Get the ship's primary gun.
     *
     * @return The ship's primary gun.
     */
    Gun getPrimary();

    /**
     * Get the ship's secondary gun.
     *
     * @return The ship's secondary gun.
     */
    Gun getSecondary();

    /**
     * Get the ship's tertiary gun.
     *
     * @return The ship's tertiary gun.
     */
    Gun getTertiary();

    /**
     * Get the ship's anti-air gun.
     *
     * @return The ship's anti-air gun.
     */
    Gun getAntiAir();

    /**
     * Get the ship's torpedo.
     *
     * @return The ship's torpedo.
     */
    Torpedo getTorpedo();

    /**
     * Get the ship's ASW capability.
     *
     * @return The ship's ASW capability.
     */
    Asw getAsw();

    /**
     * Get the ship's hull.
     *
     * @return The ship's hull.
     */
    Hull getHull();

    /**
     * Get the ship's movement.
     *
     * @return The ship's movement.
     */
    Movement getMovement();

    /**
     * Get the ship's cargo.
     *
     * @return The ship's cargo.
     */
    Cargo getCargo();

    /**
     * Get the ship's fuel.
     *
     * @return The ship's fuel.
     */
    Fuel getFuel();

    /**
     * Get the ship's ammunition type.
     *
     * @return The ship's ammunition type.
     */
    AmmunitionType getAmmunitionType();

    /**
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    List<Component> getComponents();

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
     * Set the ships ammunition type.
     *
     * @param ammunitionType The ship's ammunition type.
     */
    void setAmmunitionType(AmmunitionType ammunitionType);

    /**
     * Get the ship's persistent data.
     *
     * @return The ship's persistent data.
     */
    ShipData getData();
}
