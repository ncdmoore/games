package engima.waratsea.presenter.airfield.mission;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import lombok.Getter;

/**
 * Represents a mission's region stats.
 */
public class RegionStats {
    @Getter
    private final String title;

    @Getter
    private String minSteps;

    @Getter
    private String maxSteps;

    @Getter
    private final String currentSteps;

    @Getter
    private final String routeSteps;

    /**
     * Constructor. Use this to show a target region stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param target The mission's target.
     * @param routeSteps The steps in route or out route of the region.
     */
    public RegionStats(final Nation nation, final Target target, final int routeSteps) {
        title = "Target: " + target.getRegionTitle(nation) + " Region Details";
        maxSteps = target.getRegionMaxSteps(nation) + "";
        currentSteps = target.getRegionCurrentSteps(nation) + "";
        this.routeSteps = routeSteps + "";
    }

    /**
     * Constructor. Use this to show an airbase's region stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airbase The mission's home airbase.
     * @param routeSteps The steps in route or out route of the region.
     */
    public RegionStats(final Nation nation, final Airbase airbase, final int routeSteps) {
        title = "Airfield: " + airbase.getRegion(nation).getTitle() + " Region Details";
        minSteps = airbase.getRegion(nation).getMinSteps() + "";
        currentSteps = airbase.getRegion(nation).getCurrentSteps() + "";
        this.routeSteps = routeSteps + "";
    }

}
