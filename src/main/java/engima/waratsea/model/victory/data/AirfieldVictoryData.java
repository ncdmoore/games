package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;
import lombok.Getter;
import lombok.Setter;

/**
 * Airfield victory condition data read in from JSON file.
 */
public class AirfieldVictoryData {
    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String award;

    @Getter
    @Setter
    private AirfieldMatchData event;

    @Getter
    @Setter
    private int points;

    @Getter
    @Setter
    private int totalPoints;

    @Getter
    @Setter
    private int requiredPoints;

    @Getter
    @Setter
    private int requiredOccurences;

    @Getter
    @Setter
    private int occurrenceCount;

    @Getter
    @Setter
    private boolean requirementMet;

}
