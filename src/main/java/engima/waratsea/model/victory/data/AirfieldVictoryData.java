package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;
import lombok.Data;

/**
 * Airfield victory condition data read in from JSON file.
 */
@Data
public class AirfieldVictoryData {
    private String title;
    private String description;
    private String award;
    private AirfieldMatchData event;
    private int points;
    private int totalPoints;
    private int requiredPoints;
    private int requiredOccurences;
    private int occurrenceCount;
    private boolean requirementMet;
}
