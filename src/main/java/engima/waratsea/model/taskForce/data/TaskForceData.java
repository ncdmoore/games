package engima.waratsea.model.taskForce.data;

import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.game.event.turn.data.TurnMatchData;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.taskForce.mission.data.MissionData;
import lombok.Data;

import java.util.List;

/**
 * Contains all the task force's data that is read and written to a JSON file.
 */
@Data
public class TaskForceData {
    private String name;
    private String title;
    private MissionData mission;
    private String location;
    private List<String> possibleStartingLocations;
    private List<TargetData> targets;
    private TaskForceState state;
    private List<String> ships;
    private List<ShipMatchData> releaseShipEvents;
    private List<TurnMatchData> releaseTurnEvents;
    private List<String> cargoShips;
}
