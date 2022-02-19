package engima.waratsea.model.victory.data;

import lombok.Data;

import java.util.List;

/**
 * Represents the victory data used to determine which player wins the game.
 */
@Data
public class VictoryConditionsData {
    private String objectives;
    private int totalVictoryPoints;
    private List<ShipVictoryData> defaultShip;
    private List<ShipVictoryData> scenarioShip;
    private List<ShipVictoryData> requiredShip;
    private List<SquadronVictoryData> defaultSquadron;
    private List<SquadronVictoryData> scenarioSquadron;
    private List<AirfieldVictoryData> scenarioAirfield;
}
