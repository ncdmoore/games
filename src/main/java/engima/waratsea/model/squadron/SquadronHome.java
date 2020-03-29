package engima.waratsea.model.squadron;

import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;

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
     * Get the map reference of the squadron's home base.
     *
     * @return The squadron's home base's map reference.
     */
    String getReference();

    /**
     * Get the airfield type of the squadron's home base.
     *
     * @return The squadron's home base's airfield type.
     */
    AirfieldType getAirfieldType();


}
