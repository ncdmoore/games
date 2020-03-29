package engima.waratsea.model.base;

import engima.waratsea.model.game.Side;

/**
 * Implement this interface to provide a base in the game.
 */
public interface Base extends Comparable<Base> {
    /**
     * Get the name of the base.
     *
     * @return The name of the base.
     */
    String getName();

    /**
     * Get the title of the base.
     *
     * @return The base's title.
     */
    String getTitle();

    /**
     * The side of the airbase. This is the owning side of this airbase.
     *
     * @return The airbase side: ALLIES or AXIS.
     */
    Side getSide();

    /**
     * Get the map reference of the base.
     *
     * @return The map reference of the base.
     */
    String getReference();

    /**
     * Get the airbase's anti aircraft rating.
     *
     * @return The airbase's anti aircraft rating.
     */
    int getAntiAirRating();
}
