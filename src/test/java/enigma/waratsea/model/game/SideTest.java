package enigma.waratsea.model.game;

import engima.waratsea.model.game.Side;
import org.junit.Test;

import static engima.waratsea.model.game.Side.ALLIES;
import static engima.waratsea.model.game.Side.AXIS;

public class SideTest {

    @Test
    public void oppositeTest() {
        Side oppositeSide = ALLIES.opposite();
        assert (oppositeSide == AXIS);

    }
}
