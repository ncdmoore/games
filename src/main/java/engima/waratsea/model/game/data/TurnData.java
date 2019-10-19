package engima.waratsea.model.game.data;

import engima.waratsea.model.game.TurnIndex;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * The persistent data of the Turn class.
 */
public class TurnData {
    @Getter
    @Setter
    private int turn;            //One day equals 6 turns.

    @Getter
    @Setter
    private TurnIndex index;     //Used to determine the type of turn.

    @Getter
    @Setter
    private Date date;           //The current game date.
}
