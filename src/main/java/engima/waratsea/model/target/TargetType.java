package engima.waratsea.model.target;

import com.google.gson.annotations.SerializedName;

public enum TargetType {
    @SerializedName(value = "ENEMY_AIRFIELD", alternate = {"Enemy_Airfield", "enemy_airfield"})
    ENEMY_AIRFIELD,

    @SerializedName(value = "FRIENDLY_AIRFIELD", alternate = {"Friendly_Airfield", "friendly_airfield"})
    FRIENDLY_AIRFIELD,

    @SerializedName(value = "ENEMY_PORT", alternate = {"Enemy_Port", "enemy_port"})
    ENEMY_PORT,

    @SerializedName(value = "FRIENDLY_PORT", alternate = {"Friendly_Port", "enemy_port"})
    FRIENDLY_PORT,

    @SerializedName(value = "TASK_FORCE", alternate = {"Task_Force", "task_force"})
    TASK_FORCE,

    @SerializedName(value = "LAND_GRID", alternate = {"Land_Grid", "land_grid"})
    LAND_GRID,

    @SerializedName(value = "SEA_GRID", alternate = {"Sea_Grid", "sea_grid"})
    SEA_GRID
}
