package engima.waratsea.model.taskForce;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a task force's mission.
 */
public enum TaskForceMission {
    @SerializedName(value = "BOMBARDMENT", alternate = {"Bombardment", "bombardment"})
    BOMBARDMENT("Bombardment"),

    @SerializedName(value = "ESCORT", alternate = {"Escort", "escort"})
    ESCORT("Escort"),

    @SerializedName(value = "FERRY", alternate = {"Ferry", "ferry"})
    FERRY("Ferry"),

    @SerializedName(value = "INTERCEPT", alternate = {"Intercept", "intercept"})
    INTERCEPT("Intercept"),

    @SerializedName(value = "PATROL", alternate = {"Patrol", "patrol"})
    PATROL("Patrol"),

    @SerializedName(value = "WAIT_IN_PORT", alternate = {"Wait_in_port", "wait_in_port"})
    WAIT_IN_PORT("Wait in port"),

    @SerializedName(value = "RETREAT", alternate = {"Retreat", "retreat"})
    RETREAT("Retreat"),

    @SerializedName(value = "TRANSPORT", alternate = {"Transport", "transport"})
    TRANSPORT("Transport");

    private final String value;

    /**
     * Construct a task force mission type.
     * @param value The string value. Used in the GUI.
     */
    TaskForceMission(final String value) {
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
