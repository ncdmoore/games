package engima.waratsea.model.ship;

/**
 * Represents a ship component that has health rating.
 */
public interface Component {
    /**
     * Get the component's name.
     *
     * @return The component's name.
     */
    String getName();

    /**
     * Determine if the component is present.
     *
     * @return True if the component is present. False otherwise.
     */
    boolean isPresent();

    /**
     * Get the max health of the component.
     *
     * @return The component max health.
     */
    int getMaxHealth();

    /**
     * Get the health of the component.
     *
     * @return The component health.
     */
    int getHealth();
}
