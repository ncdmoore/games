package engima.waratsea.model.squadron;

import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.region.Region;

import java.util.Optional;

public interface SquadronHome {

    /**
     * Get the name of the squadron's home base.
     *
     * @return The name of the squadron's home base.
     */
    String getName();

    /**
     * Get the title of the squadron's home base.
     *
     * @return The title of the squadron's home base.
     */
    String getTitle();

    /**
     * Get the region of the squadron's home base.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The squadron's home base's region.
     */
    Region getRegion(Nation nation);

    /**
     * Get the region's title. The regions title should be independent of the nation. If nations share a region, the
     * region is represented by a separate java region java object for each nation. This is because each nation's region
     * has separate requirements. However, the actual map region is the same. Thus the title is the same.
     *
     * @return The region's title.
     */
    String getRegionTitle();

    /**
     * Get the map reference of the squadron's home base.
     *
     * @return The squadron's home base's map reference.
     */
    String getReference();

    /**
     * Get the squadron's home game grid.
     *
     * @return The squadron's home game grid.
     */
    Optional<GameGrid> getGrid();

    /**
     * Get the airfield type of the squadron's home base.
     *
     * @return The squadron's home base's airfield type.
     */
    AirfieldType getAirfieldType();


}
