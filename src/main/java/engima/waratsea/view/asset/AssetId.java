package engima.waratsea.view.asset;

import engima.waratsea.model.game.AssetType;
import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

public class AssetId {
    @Getter private final AssetType type;
    @Getter private final String name;

    /**
     * Constructor.
     *
     * @param type The type of asset.
     * @param name The name of the asset.
     */
    public AssetId(@NonNull final AssetType type, @NonNull final String name) {
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

    /**
     * Determine if two asset id's are equal.
     *
     * @param o The other asset id.
     * @return True if this asset id and the other asset id are equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof AssetId)) {
            return false;
        }

        if (o == this) {
            return true;
        }

        AssetId otherAssetId = (AssetId) o;

        return type == otherAssetId.type && name.equals(otherAssetId.name);
    }

    /**
     * Get the hash code for this class.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }
}
