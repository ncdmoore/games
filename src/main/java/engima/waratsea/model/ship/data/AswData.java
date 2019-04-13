package engima.waratsea.model.ship.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Models the Anti Submarine capability of the ship. Data is read in from a JSON file.
 */
public class AswData {
    @Getter
    @Setter
    private boolean capable;
}
