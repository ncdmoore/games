package engima.waratsea.model.player;

import engima.waratsea.model.game.Side;

/**
 * Represents players in the game. Note, there are only two players/sides: ALLIES and AXIS.
 */
public interface Player {

    /**
     * This sets the side of the player.
     * @param side ALLIES or AXIS.
     */
    void setSide(Side side);
}
