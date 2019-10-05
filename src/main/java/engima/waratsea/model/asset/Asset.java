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
     * Get the title of the asset that is displayed on the GUI.
     *
     * @return The asset title.
     */
    String getTitle();

    /**
     * The map reference of the asset.
     *
     * @return The map reference of the asset.
     */
    String getReference();

    /**
     * Get the active state of the asset.
     *
     * @return True if the asset is active. False if the asset is not active.
     */
    boolean isActive();
}
