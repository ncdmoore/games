package engima.waratsea.model.game;

import com.google.gson.annotations.SerializedName;

/**
 * The game nations.
 */
public enum Nation {
    @SerializedName(value = "AUSTRALIAN", alternate = {"Australian", "australian"})
    AUSTRALIAN,

    @SerializedName(value = "BRITISH", alternate = {"British", "british"})
    BRITISH,

    @SerializedName(value = "FRENCH", alternate = {"French", "french"})
    FRENCH,

    @SerializedName(value = "GERMAN", alternate = {"German", "german"})
    GERMAN,

    @SerializedName(value = "GREEK", alternate = {"Greek", "greek"})
    GREEK,

    @SerializedName(value = "ITALIAN", alternate = {"Italian", "italian"})
    ITALIAN,

    @SerializedName(value = "POLISH", alternate = {"Polish", "polish"})
    POLISH
}
