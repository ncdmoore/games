package engima.waratsea.model.taskForce.data;

import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.turn.TurnEventMatcher;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForceMission;
import engima.waratsea.model.taskForce.TaskForceState;
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
    private TaskForceMission mission;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private List<Target> targets;

    @Getter
    @Setter
    private TaskForceState state;

    @Getter
    @Setter
    private List<String> ships;

    @Getter
    @Setter
    private List<ShipEventMatcher> releaseShipEvents;

    @Getter
    @Setter
    private List<TurnEventMatcher> releaseTurnEvents;

    @Getter
    @Setter
    private List<String> cargoShips;

}
