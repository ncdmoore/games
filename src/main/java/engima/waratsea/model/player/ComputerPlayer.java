package engima.waratsea.model.player;

import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

import java.util.List;

/**
 * This is the computer player in the game.
 */
public class ComputerPlayer implements Player {

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private List<TaskForce> taskForces;
}
