package engima.waratsea.model.taskForce;

import engima.waratsea.event.ship.ShipEvent;
import engima.waratsea.event.turn.RandomTurnEvent;
import engima.waratsea.event.turn.TurnEvent;
import engima.waratsea.model.target.Target;
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
    private List<ShipEvent> releaseShipEvents;

    @Getter
    @Setter
    private List<TurnEvent> releaseTurnEvents;

    @Getter
    @Setter
    private List<RandomTurnEvent> releaseRandomTurnEvents;
}
