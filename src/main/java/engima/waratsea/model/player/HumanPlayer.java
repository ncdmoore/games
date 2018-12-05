package engima.waratsea.model.player;

import com.google.inject.Inject;
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

    /**
     * Constructor called by guice.
     * @param shipEventHandler Handles all ship events for this player.
     */
    @Inject
    public HumanPlayer(final ShipEventHandler shipEventHandler) {
        shipEventHandler.setPlayer(this);
    }
}
