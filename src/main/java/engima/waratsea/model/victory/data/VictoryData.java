package engima.waratsea.model.victory.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the victory data used to determine which player wins the game.
 */
public class VictoryData {
    @Getter
    @Setter
    private List<ShipVictoryData> ship;
}
