package engima.waratsea.model.base;

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
     * Get the map reference of the base.
     *
     * @return The map reference of the base.
     */
    String getReference();
}
