package engima.waratsea.model.player;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.TaskForce;

import java.util.List;

/**
 * Represents players in the game. Note, there are only two players/sides: ALLIES and AXIS.
 */
public interface Player {


    /**
     * This gets teh side of the player.
     * @return The player's side ALLIES or AXIS.
     */
    Side getSide();

    /**
     * This sets the side of the player.
     * @param side ALLIES or AXIS.
     */
    void setSide(Side side);

    /**
     * This sets the player's task forces.
     * @param taskForces The player's task forces.
     */
    void setTaskForces(List<TaskForce> taskForces);


    /**
     * This gets the player's task forces.
     * @return The player's task forces.
     */
    List<TaskForce> getTaskForces();
}
