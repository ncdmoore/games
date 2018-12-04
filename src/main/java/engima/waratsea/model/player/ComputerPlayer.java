package engima.waratsea.model.player;

import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

/**
 * This is the computer player in the game.
 */
public class ComputerPlayer implements Player {

    @Getter
    @Setter
    private Side side;
}
