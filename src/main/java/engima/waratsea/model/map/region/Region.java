package engima.waratsea.model.map.region;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.data.RegionData;
import lombok.Getter;

import java.util.List;

/**
 * Represents a map region within the game.
 */
public class Region {

    @Getter
    private final String name;

    @Getter
    private final Nation nation;

    @Getter
    private final String min;

    @Getter
    private final String max;

    @Getter
    private final List<String> airfields;

    @Getter
    private final List<String> ports;

    /**
     * Constructor of Task Force called by guice.
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     */
    @Inject
    public Region(@Assisted final Side side,
                  @Assisted final RegionData data) {

        name = data.getName();
        nation = data.getNation();
        min = data.getMin();
        max = data.getMax();
        airfields = data.getAirfields();
        ports = data.getPorts();
    }
}
