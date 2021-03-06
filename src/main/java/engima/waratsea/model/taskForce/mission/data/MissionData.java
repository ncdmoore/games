package engima.waratsea.model.taskForce.mission.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The persistent mission data.
 */
public class MissionData {

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private SeaMissionType type;

    @Getter
    @Setter
    private List<TargetData> targets;
}
