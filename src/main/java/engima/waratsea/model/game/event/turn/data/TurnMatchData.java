package engima.waratsea.model.game.event.turn.data;

import lombok.Data;

import java.util.Set;

/**
 * Represents the data used to match turn events. This class is read from a JSON file.
 */
@Data
public class TurnMatchData {
    private int turn;
    private int turnGreaterThan;
    private Set<Integer> values;
}
