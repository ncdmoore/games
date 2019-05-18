package engima.waratsea.model.squadron.deployment;

import com.google.gson.annotations.SerializedName;

/**
 * This class indicates the type of squadron deployment.
 *
 * COMPUTER indicates that the squadron deployment is done by AI (the computer)
 * HUMAN indicates that the squadron deployment is done by the human.
 */
public enum SquadronDeploymentType {
    @SerializedName(value = "COMPUTER", alternate = {"Computer", "computer"})
    COMPUTER,

    @SerializedName(value = "HUMAN", alternate = {"Human", "human"})
    HUMAN
}
