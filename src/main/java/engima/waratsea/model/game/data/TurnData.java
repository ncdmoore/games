package engima.waratsea.model.game.data;

import engima.waratsea.model.game.TurnIndex;
import lombok.Data;

import java.util.Date;

/**
 * The persistent data of the Turn class.
 */
@Data
public class TurnData {
    private int turn;            //One day equals 6 turns.
    private TurnIndex index;     //Used to determine the type of turn.
    private Date date;           //The current game date.
}
