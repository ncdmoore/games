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
    private List<TaskForce> taskForces;


    /**
     * Set the players task forces and parse into active and reserve groups.
     * @param taskForces The players task forces.
     */
    public void setTaskForces(final List<TaskForce> taskForces) {
        this.taskForces = taskForces;

        this.taskForces.forEach(TaskForce::registerEvents);
    }

}
