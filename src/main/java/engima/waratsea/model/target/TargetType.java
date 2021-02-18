package engima.waratsea.model.target;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum TargetType {
    @SerializedName(value = "ENEMY_AIRFIELD", alternate = {"Enemy_Airfield", "enemy_airfield"})
    ENEMY_AIRFIELD,

    @SerializedName(value = "FRIENDLY_AIRBASE", alternate = {"Friendly_Airbase", "friendly_airbase"})
    FRIENDLY_AIRBASE,

    @SerializedName(value = "ENEMY_PORT", alternate = {"Enemy_Port", "enemy_port"})
    ENEMY_PORT,

    @SerializedName(value = "FRIENDLY_PORT", alternate = {"Friendly_Port", "enemy_port"})
    FRIENDLY_PORT,

    @SerializedName(value = "ENEMY_TASK_FORCE", alternate = {"Enemy_Task_Force", "enemy_task_force"})
    ENEMY_TASK_FORCE,

    @SerializedName(value = "FRIENDLY_TASK_FORCE", alternate = {"Friendly_Task_Force", "friendly_task_force"})
    FRIENDLY_TASK_FORCE,

    @SerializedName(value = "LAND_GRID", alternate = {"Land_Grid", "land_grid"})
    LAND_GRID,

    @SerializedName(value = "SEA_GRID", alternate = {"Sea_Grid", "sea_grid"})
    SEA_GRID;

    private static final Map<Class<?>, String> TITLE_MAP = new HashMap<>();

    static {
        TITLE_MAP.put(TargetEnemyAirfield.class, "Airfield");
        TITLE_MAP.put(TargetEnemyPort.class, "Port");
        TITLE_MAP.put(TargetEnemyTaskForce.class, "Task Force");
        TITLE_MAP.put(TargetFriendlyAirbase.class, "Airbase");
        TITLE_MAP.put(TargetFriendlyPort.class, "Port");
        TITLE_MAP.put(TargetFriendlyTaskForce.class, "Task Force");
        TITLE_MAP.put(TargetLandGrid.class, "Land Grid");
        TITLE_MAP.put(TargetSeaGrid.class, "Sea Grid");
    }

    /**
     * Get the target's title from its class.
     *
     * @param target A target.
     * @return The corresponding title.
     */
    public static String getTitle(final Target target) {
        return TITLE_MAP.get(target.getClass());
    }

    /**
     * Get a stream of this enum's values.
     *
     * @return A stream of this enum's values.
     */
    public static Stream<TargetType> stream() {
        return Stream.of(TargetType.values());
    }
}
