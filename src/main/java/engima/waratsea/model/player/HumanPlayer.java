package engima.waratsea.model.player;

import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

    @Getter
    @Setter
    private Side side;
}
