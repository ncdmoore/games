package engima.waratsea.model.victory.data;

import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is the data for a required victory condition that is read in from a JSON file.
 */
public class RequiredShipVictoryData {
    @Getter
    @Setter
    private List<ShipMatchData> events;
}
