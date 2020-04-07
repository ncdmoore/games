package engima.waratsea.view.asset;

import engima.waratsea.model.game.AssetType;
import lombok.Getter;

public class AssetId {
    @Getter private final AssetType type;
    @Getter private final String name;

    /**
     * Constructor.
     *
     * @param type The type of asset.
     * @param name The name of the asset.
     */
    public AssetId(final AssetType type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Get the asset Id's key.
     *
     * @return The asset Id's key.
     */
    public String getKey() {
        return type + name;
    }
}
