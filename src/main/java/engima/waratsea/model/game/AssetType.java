package engima.waratsea.model.game;

import com.google.gson.annotations.SerializedName;

/**
 * Game assests.
 */
public enum AssetType {
    @SerializedName(value = "AIRCRAFT", alternate = {"Aircraft", "aircraft"})
    AIRCRAFT,

    @SerializedName(value = "SHIP", alternate = {"Ship", "ship"})
    SHIP,

    @SerializedName(value = "SUB", alternate = {"Sub", "sub"})
    SUB
}
