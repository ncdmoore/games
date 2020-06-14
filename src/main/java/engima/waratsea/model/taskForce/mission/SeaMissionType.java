package engima.waratsea.model.taskForce.mission;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a task force's mission.
 */
public enum SeaMissionType {
    @SerializedName(value = "AIR_RAID", alternate = {"Air_Raid", "air_raid"})
    AIR_RAID("Air Raid"),

    @SerializedName(value = "BOMBARDMENT", alternate = {"Bombardment", "bombardment"})
    BOMBARDMENT("Bombardment"),

    @SerializedName(value = "ESCORT", alternate = {"Escort", "escort"})
    ESCORT("Escort"),

    @SerializedName(value = "FERRY", alternate = {"Ferry", "ferry"})
    FERRY("Ferry"),

    @SerializedName(value = "FERRY_AIRCRAFT", alternate = {"Ferry_Aircraft", "ferry_aircraft"})
    FERRY_AIRCRAFT("Ferry Aircraft"),

    @SerializedName(value = "MINELAYING", alternate = {"Minelaying, minelaying"})
    MINELAYING("Minelaying"),

    @SerializedName(value = "MINESWEEPING", alternate = {"MineSweeping, mineSweeping"})
    MINESWEEPING("MineSweeping"),

    @SerializedName(value = "INTERCEPT", alternate = {"Intercept", "intercept"})
    INTERCEPT("Intercept"),

    @SerializedName(value = "INVASION", alternate = {"Invasion", "invasion"})
    INVASION("Invasion"),

    @SerializedName(value = "PATROL", alternate = {"Patrol", "patrol"})
    PATROL("Patrol"),

    @SerializedName(value = "RETREAT", alternate = {"Retreat", "retreat"})
    RETREAT("Retreat"),

    @SerializedName(value = "TRANSPORT", alternate = {"Transport", "transport"})
    TRANSPORT("Transport");

    private final String value;

    /**
     * Construct a task force mission type.
     * @param value The string value. Used in the GUI.
     */
    SeaMissionType(final String value) {
        this.value = value;
    }

    /**
     * The string representation of the task force mission.
     * @return The string value of the task force mission.
     */
    @Override
    public String toString() {
        return value;
    }
}
