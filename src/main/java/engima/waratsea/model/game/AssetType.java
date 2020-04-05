package engima.waratsea.model.game;

import com.google.gson.annotations.SerializedName;

/**
 * Game assets.
 */
public enum AssetType {
    AIRFIELD,

    @SerializedName(value = "AIRCRAFT", alternate = {"Aircraft", "aircraft"})
    AIRCRAFT,

    @SerializedName(value = "SHIP", alternate = {"Ship", "ship"})
    SHIP,

    @SerializedName(value = "SUB", alternate = {"Sub", "sub"})
    SUB
}
