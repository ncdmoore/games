package engima.waratsea.model.victory.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the victory data used to determine which player wins the game.
 */
public class VictoryConditionsData {
    @Getter
    @Setter
    private String objectives;

    @Getter
    @Setter
    private List<ShipVictoryData> ship;

    @Getter
    @Setter
    private List<ShipVictoryData> requiredShip;
}
