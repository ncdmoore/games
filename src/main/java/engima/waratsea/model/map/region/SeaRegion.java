package engima.waratsea.model.map.region;

import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Represents all seas in the game. Since ships with squadrons implement the airbase interface these ships need a
 * region. This is the region that is used. This just keeps things simple and no special code is needed for ships
 * that implement the airbase interface.
 *
 * Note, this is a singleton class.
 */
@Singleton
public class SeaRegion implements Region {
    @Getter private final String name = "Sea";
    @Getter private final String title = name;
    @Getter private final Side side = Side.NEUTRAL;
    @Getter private final Nation nation = null;
    @Getter private final List<Airfield> airfields = Collections.emptyList();
    @Getter private final List<Port> ports = Collections.emptyList();
    @Getter private final int minSteps = 0;
    @Getter private final int maxSteps = 0;
    @Getter private final String mapRef = "";
    @Getter private final int needed = 0;
    @Getter private final int currentSteps = 0;

    /**
     * Determine the minimum and maximum step requirements for this region for a given nation. We cannot just
     * determine these values from data read in a JSON file as it is possible for these values to depend
     * upon the total number of squadrons that a particular nation has. Thus, we must delay the calculation
     * of the minimum and maximum step requirements for a region until the total number of squadrons a nation
     * has is determined. Note, the total number of squadrons may vary (independent of scenario) each time a
     * game is played.
     *
     * @param squadrons The nation's squadrons.
     * @return The region is returned.
     */
    @Override
    public Region setRequirements(final List<Squadron> squadrons) {
        return this;
    }

    /**
     * Determine if this region has a minimum squadron deployment requirement.
     *
     * @return True if this region has a minimum squadron deployment requirement. False otherwise.
     */
    @Override
    public boolean hasMinimumRequirement() {
        return false;
    }

    /**
     * Determine if this region has roon to add a squadron.
     *
     * @param squadron The potential squadron to add.
     * @return True if the squadron may be added. False otherise.
     */
    @Override
    public boolean hasRoom(final Squadron squadron) {
        return true;
    }

    /**
     * Determine if this region's minimum squadron requirement is met.
     *
     * @return True if this region's minimum squadron requirement is met. False if this region's mininum
     * squadron requirement is not yet met.
     */
    @Override
    public boolean minimumSatisfied() {
        return true;
    }

    /**
     * Determine if this region's minimum squadron requirement is still met if the given number
     * of squadron steps is removed from this region.
     *
     * @param removedSteps The number of squadron steps to remove from this region.
     * @return True if the given squadron steps can be removed from this region and the region's mimimun
     * squadron step requirement is still satisfied. False otherwise.
     */
    @Override
    public boolean minimumSatisfied(final int removedSteps) {
        return true;
    }
}
