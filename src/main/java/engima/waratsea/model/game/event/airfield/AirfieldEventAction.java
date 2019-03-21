package engima.waratsea.model.game.event.airfield;

import com.google.gson.annotations.SerializedName;

/**
 * Airfield event actions.
 */
public enum AirfieldEventAction {
    @SerializedName(value = "DAMAGE", alternate = {"Damage", "damage"})
    DAMAGE("damage"),

    @SerializedName(value = "REPAIR", alternate = {"Repair", "repair"})
    REPAIR("repair");

    private String value;

    /**
     * Constructor.
     * @param value String value of the enum.
     */
    AirfieldEventAction(final String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the enum.
     * @return The enum's string value.
     */
    @Override
    public String toString() {
        return value;
    }
}
