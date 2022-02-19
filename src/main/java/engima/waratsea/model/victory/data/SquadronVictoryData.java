package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.squadron.data.SquadronMatchData;
import lombok.Data;

/**
 * Squadron victory condition data read in from JSON file.
 */
@Data
public class SquadronVictoryData {
    private String title;
    private String description;
    private String award;
    private SquadronMatchData event;
    private int points;
    private int totalPoints;
    private int requiredPoints;
    private int requiredOccurences;
    private int occurrenceCount;
    private boolean requirementMet;
}
