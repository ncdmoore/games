package engima.waratsea.model.asset;

import engima.waratsea.model.game.Side;

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
     * Get the side of the asset.
     *
     * @return The side of the asset.
     */
    Side getSide();

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
