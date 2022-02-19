package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import lombok.Data;

import java.util.List;

/**
 * Ship victory condition data read in from JSON file.
 */
@Data
public class ShipVictoryData {
    private String title;
    private String description;
    private String award;
    private ShipMatchData event;
    private int points;
    private int totalPoints;
    private int requiredPoints;
    private int requiredOccurrences;
    private int occurrenceCount;
    private boolean requirementMet;
    private List<ShipMatchData> events;
}
