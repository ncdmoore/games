package engima.waratsea.model.player;

import engima.waratsea.model.ships.TaskForce;
import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

import java.util.List;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private List<TaskForce> taskForces;
}
