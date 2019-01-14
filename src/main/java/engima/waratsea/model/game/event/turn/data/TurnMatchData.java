package engima.waratsea.model.game.event.turn.data;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * Represents the data used to match turn events. This class is read from a JSON file.
 */
public class TurnMatchData {
    @Getter
    @Setter
    private int turn;

    @Getter
    @Setter
    private int turnGreaterThan;

    @Getter
    @Setter
    private Set<Integer> values;
}
