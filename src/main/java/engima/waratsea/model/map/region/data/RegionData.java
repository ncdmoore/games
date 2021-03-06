package engima.waratsea.model.map.region.data;

import engima.waratsea.model.game.Nation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents map region data that is written to and from JSON.
 */
public class RegionData {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Nation nation;

    @Getter
    @Setter
    private String min; // in steps.

    @Getter
    @Setter
    private String max; // in steps.

    @Getter
    @Setter
    private List<String> airfields;

    @Getter
    @Setter
    private List<String> ports;

    @Getter
    @Setter
    private String mapRef;
}
