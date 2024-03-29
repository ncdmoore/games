package engima.waratsea.model.taskForce;

import com.google.gson.annotations.SerializedName;
import lombok.RequiredArgsConstructor;

/**
 * Represents a task force state.
 * Active task forces are available for the player to control and use.
 * Reserve task forces are in reserve and are not available to the player.
 * Certain game events will trigger a task force to change states from reserve to active.
 */
@RequiredArgsConstructor
public enum TaskForceState {
    @SerializedName(value = "ACTIVE", alternate = {"Active", "active"})
    ACTIVE("Active"),

    @SerializedName(value = "RESERVE", alternate = {"Reserve", "reserve"})
    RESERVE("Reserve");

    private final String value;

    /**
     * Return the string value of the enum.
     */
    @Override
    public String toString() {
        return value;
    }
}
