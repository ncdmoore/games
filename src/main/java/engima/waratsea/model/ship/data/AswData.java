package engima.waratsea.model.ship.data;

import lombok.Data;

/**
 * Models the Anti Submarine capability of the ship. Data is read in from a JSON file.
 */
@Data
public class AswData {
    private boolean capable;
}
