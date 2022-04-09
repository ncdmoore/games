package engima.waratsea.model.base.port.data;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import lombok.Data;

/**
 * Represents port data that is read in from a JSON file.
 */
@Data
public class PortData {
    private String name;
    private Side side;
    private Region region;
    private String size;
    private int antiAir;
    private String location;
}
