package engima.waratsea.model.asset;

/**
 * Represents an asset in the game.
 */
public interface Asset {
    /**
     * Get the name of the asset.
     *
     * @return The name of the asset.
     */
    String getName();

    /**
     * The map location of the asset.
     *
     * @return The map location of the asset.
     */
    String getLocation();

    /**
     * Get the active state of the asset.
     *
     * @return True if the asset is active. False if the asset is not active.
     */
    boolean isActive();
}
