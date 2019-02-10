package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import lombok.Getter;
import lombok.Setter;

/**
 * Ship victory condition data read in from JSON file.
 */
public class ShipVictoryData {

    @Getter
    @Setter
    private ShipMatchData event;

    @Getter
    @Setter
    private int points;

    @Getter
    @Setter
    private int requiredPoints;
}
