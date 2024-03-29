package engima.waratsea.model.map.region;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronStrength;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a map region within the game.
 *
 * Each nation has its on set of regions within the game. To uniquely identify a region both the name of the region
 * and the nation of the region must be specified. A region contains a set of airfields and ports.
 * Note, that a given airfield or port if used by more than one nation, may be shared by several regions. For
 * example, the Sicily region is used by both Germany and Italy. Therefore, there are two regions for Sicily:
 * one German and one Italian. All the airfields in Sicily used by both nations will be in both regions.
 */
@Slf4j
public class LandRegion implements Region {
    @Getter private final String name;
    @Getter private final Side side;
    @Getter private final Nation nation;

    private final String minStepsString;
    private final String maxStepsString;

    @Getter private final List<Airfield> airfields;
    @Getter private final List<Port> ports;
    @Getter private int minSteps;
    @Getter private int maxSteps;

    @Getter private final String mapRef;  //This is the regions central game grid.
                                          //Used to mark the region on the game map

    /**
     * Constructor of Region called by guice.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param airfieldDAO The data abstraction object for airfields. Loads the airfields.
     * @param portDAO The data abstraction object for ports. Loads the ports.
     */
    @Inject
    public LandRegion(@Assisted final Side side,
                      @Assisted final RegionData data,
                      final AirfieldDAO airfieldDAO,
                      final PortDAO portDAO) {

        name = data.getName();
        this.side = side;
        nation = data.getNation();
        minStepsString = data.getMin();
        maxStepsString = data.getMax();
        mapRef = data.getMapRef();

        airfields = Optional.ofNullable(data.getAirfields())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(this::buildBaseId)
                .map(airfieldDAO::load)
                .collect(Collectors.toList());

        airfields.forEach(airfield -> airfield.addRegion(this));

        nation.setSquadronsPresent(!airfields.isEmpty());

        ports = Optional.ofNullable(data.getPorts())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(this::buildBaseId)
                .map(portDAO::load)
                .collect(Collectors.toList());
    }

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
        // Get the nation's total steps.
        int totalAircraft = squadrons
                .stream()
                .map(Squadron::getAircraftNumber)
                .reduce(0, Integer::sum);

        int totalSteps = SquadronStrength.calculateSteps(totalAircraft);

        minSteps = determineValue(totalSteps, minStepsString);  //Set the minimum steps that must be deployed in this region.
        maxSteps = determineValue(totalSteps, maxStepsString);  //Set the maximum steps that may be deployed in this region.
        return this;
    }

    /**
     * Determine if this region has a minimum squadron deployment requirement.
     *
     * @return True if this region has a minimum squadron deployment requirement. False otherwise.
     */
    @Override
    public boolean hasMinimumRequirement() {
        return minSteps > 0;
    }

    /**
     * Determine if this region's minimum squadron deployment requirement requires that additional
     * squadrons be deployed.
     *
     * @return The number of steps needed to fulfill this region's minimum squadron deployment requirement.
     */
    @Override
    public int getNeeded() {
        int neededSteps = minSteps - getCurrentSteps();
        return Math.max(neededSteps, 0);
    }

    /**
     * Determine if this region has room to add a squadron.
     *
     * @param squadron The potential squadron to add.
     * @return True if the squadron may be added. False otherwise.
     */
    @Override
    public boolean hasRoom(final Squadron squadron) {

        if (maxSteps == 0) {
            log.debug("Region: '{}' has room result: true.", name);
            return true; //If maxSteps is zero, then this region has no maximum.
        }

        int steps = squadron.getSteps();

        boolean result = steps + getCurrentSteps() <= maxSteps;

        log.debug("Region: '{}' has room result: {}", name, result);

        if (!result) {
            log.warn("Region: '{}' has maxSteps: {} with current: {}", new Object[]{name, maxSteps, getCurrentSteps()});
        }

        return result;
    }

    /**
     * Determine if this region's minimum squadron requirement is met.
     *
     * @return True if this region's minimum squadron requirement is met. False if this region's minimum
     * squadron requirement is not yet met.
     */
    @Override
    public boolean minimumSatisfied() {
        return getNeeded() == 0;
    }

    /**
     * Determine if this region's minimum squadron requirement is still met if the given number
     * of squadron steps is removed from this region.
     *
     * @param removedSteps The number of squadron steps to remove from this region.
     * @return True if the given squadron steps can be removed from this region and the region's minimum
     * squadron step requirement is still satisfied. False otherwise.
     */
    @Override
    public boolean minimumSatisfied(final int removedSteps) {
        return getCurrentSteps() - removedSteps >= minSteps;
    }

    /**
     * Determine the current number of steps stationed in this region.
     *
     * @return The current number of steps stationed in this region.
     */
    @Override
    public int getCurrentSteps() {
        int totalNumberOfAircraft = airfields
                .stream()
                .flatMap(airfield -> airfield.getSquadrons().stream())
                .filter(squadron -> squadron.ofNation(nation))
                .map(Squadron::getAircraftNumber)
                .reduce(0, Integer::sum);

        return SquadronStrength.calculateSteps(totalNumberOfAircraft);
    }

    /**
     * Get the region's title.
     *
     * @return The region's title.
     */
    @Override
    public String getTitle() {
        return getName();
    }

    /**
     * The String representation of the region.
     *
     * @return String representation of the region.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Build an airfield Id. This uniquely identifies an airfield.
     *
     * @param baseId The name of the airfield.
     * @return An airfield id.
     */
    private BaseId buildBaseId(final String baseId) {
        return new BaseId(baseId, side);
    }

    /**
     * Determine the step requirement for this region.
     *
     * @param totalSteps The total steps for a given nation.
     * @param value The minSteps or maxSteps step requirement for this region for a given nation.
     * @return The minSteps or maxSteps step requirement.
     */
    private int determineValue(final int totalSteps, final String value) {
        if ("HALF".equalsIgnoreCase(value)) {
            return totalSteps / 2;
        }

        return Integer.parseInt(value);
    }
}
