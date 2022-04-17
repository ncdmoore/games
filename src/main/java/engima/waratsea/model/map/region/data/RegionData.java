package engima.waratsea.model.map.region.data;

import engima.waratsea.model.game.Nation;
import lombok.Data;

import java.util.List;

/**
 * Represents map region data that is written to and from JSON.
 */
@Data
public class RegionData {
    private String name;
    private Nation nation;
    private String min; // in steps.
    private String max; // in steps.
    private List<String> airfields;
    private List<String> ports;
    private String mapRef;
}
