package engima.waratsea.model.map.region;

import com.google.inject.name.Named;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.data.RegionData;

/**
 * Factory used by guice to create regions.
 */
public interface RegionFactory {
    /**
     * Creates a map region.
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Region data read from a JSON file.
     * @return A Region initialized with the data from the JSON file.
     */
    @Named("land")
    Region createLandRegion(Side side, RegionData data);
}
