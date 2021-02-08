package engima.waratsea.model.map.region;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;

import java.util.List;

public interface Region {
    /**
     * Get the name of the region.
     *
     * @return The region's name.
     */
    String getName();

    /**
     * Get the side of the region.
     *
     * @return The side: ALLIES or AXIS.
     */
    Side getSide();

    /**
     * Get the nation of the region. Regions are per nation. If two nations share a region, then there is a
     * region for each nation. For example, both Germany and Italy share the region of Sicily. Thus, there
     * are two regions of Sicily: one German and one Italian.
     *
     * @return The nation: BRITISH, ITALIAN, etc.
     */
    Nation getNation();

    /**
     * Get the airfields contained within this region.
     *
     * @return The airfields contained within this region.
     */
    List<Airfield> getAirfields();

    /**
     * Get the ports contained within this region.
     *
     * @return The ports contained within this region.
     */
    List<Port> getPorts();

    /**
     * Get the minimum required steps needed by this region. The nation must station this number of
     * squadrons at all times within this region.
     *
     * @return The minimum number of steps that must be stationed in this region.
     */
    int getMinSteps();

    /**
     * Get the maximum number of steps that may be stationed in this region. The nation cannot station
     * more steps than this number in this region.
     *
     * @return The maximum number of steps that may be statiioned in this region.
     */
    int getMaxSteps();

    /**
     * Get the region's label map reference. Regions may be shown on the main game map. This map reference
     * indicates where the region label is displayed on the main game map.
     *
     * @return The region's label's game map reference.
     */
    String getMapRef();

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
    Region setRequirements(List<Squadron> squadrons);

    /**
     * Determine if this region has a minimum squadron deployment requirement.
     *
     * @return True if this region has a minimum squadron deployment requirement. False otherwise.
     */
    boolean hasMinimumRequirement();

    /**
     * Determine if this region's minimum squadron deployment requirement requires that additional
     * squadrons be deployed.
     *
     * @return The number of steps needed to fulfill this region's minimum squadron deployment requirement.
     */
    int getNeeded();

    /**
     * Determine if this region has roon to add a squadron.
     *
     * @param squadron The potential squadron to add.
     * @return True if the squadron may be added. False otherise.
     */
    boolean hasRoom(Squadron squadron);

    /**
     * Determine if this region's minimum squadron requirement is met.
     *
     * @return True if this region's minimum squadron requirement is met. False if this region's mininum
     * squadron requirement is not yet met.
     */
    boolean minimumSatisfied();

    /**
     * Determine if this region's minimum squadron requirement is still met if the given number
     * of squadron steps is removed from this region.
     *
     * @param removedSteps The number of squadron steps to remove from this region.
     * @return True if the given squadron steps can be removed from this region and the region's mimimun
     * squadron step requirement is still satisfied. False otherwise.
     */
    boolean minimumSatisfied(int removedSteps);

    /**
     * Determine the current number of steps stationed in this region.
     *
     * @return The current number of steps stationed in this region.
     */
    int getCurrentSteps();

    /**
     * Get the region's title.
     *
     * @return The region's title.
     */
    String getTitle();








}
