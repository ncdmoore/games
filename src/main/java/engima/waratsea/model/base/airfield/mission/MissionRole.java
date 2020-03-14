package engima.waratsea.model.base.airfield.mission;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public enum MissionRole {
    @SerializedName(value = "MAIN", alternate = {"Main", "main"})
    MAIN("Main"),

    @SerializedName(value = "ESCORT", alternate = {"Escort", "escort"})
    ESCORT("Escort");

    @Getter
    private String value;

    /**
     * Constructor.
     *
     * @param value The String representation of this enum.
     */
    MissionRole(final String value) {
        this.value = value;
    }

    /**
     * The String representation of this enum.
     *
     * @return The String representation of this enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
