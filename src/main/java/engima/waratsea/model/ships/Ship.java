package engima.waratsea.model.ships;

import engima.waratsea.model.game.Nation;

/**
 * Represents a ship.
 */
public interface Ship {

    /**
     * Get the ship's name.
     * @return The ship's name.
     */
    String getName();

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
}
