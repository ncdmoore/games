package engima.waratsea.model.taskForce.mission.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import lombok.Data;

import java.util.List;

/**
 * The persistent mission data.
 */
@Data
public class MissionData {
    private Side side;
    private SeaMissionType type;
    private List<TargetData> targets;
}
