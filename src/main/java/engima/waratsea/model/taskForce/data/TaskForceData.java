package engima.waratsea.model.taskForce.data;

import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.taskForce.mission.data.MissionData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Contains all of the task force's data that is read and written to a JSON file.
 */
public class TaskForceData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private MissionData mission;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private List<TargetData> targets;

    @Getter
    @Setter
    private TaskForceState state;

    @Getter
    @Setter
    private List<String> ships;

    @Getter
    @Setter
    private List<ShipMatchData> releaseShipEvents;

    @Getter
    @Setter
    private List<TurnMatchData> releaseTurnEvents;

    @Getter
    @Setter
    private List<String> cargoShips;

}
