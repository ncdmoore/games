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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents a map region within the game.
 *
 * A region may be shared by nations. Or each nation may have its on region object with unique properties.
 * For this latter case, the map JSON file will contain two separate region definitions each with the same name.
 * For the former case, a single region definition will exist in which multiple nations are defined in its
 * nation's list (array).
 */
@Slf4j
public class Region {

    @Getter
    private final String name;

    @Getter
    private final Side side;

    @Getter
    private final List<Nation> nations;

    private final String minString; // in steps.
    private final String maxString; // in steps.

    @Getter
    private final List<Airfield> airfields;

    @Getter
    private final List<Port> ports;

    @Getter
    private int min;  // in steps.

    @Getter
    private int max;  // in steps.

    /**
     * Constructor of Region called by guice.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data The task force data read from a JSON file.
     * @param airfieldDAO The data abstraction object for airfields. Loads the airfields.
     * @param portDAO The data abstraction objectg for ports. Loads the ports.
     */
    @Inject
    public Region(@Assisted final Side side,
                  @Assisted final RegionData data,
                  final AirfieldDAO airfieldDAO,
                  final PortDAO portDAO) {

        name = data.getName();
        this.side = side;
        nations = data.getNation();
        minString = data.getMin();
        maxString = data.getMax();

        airfields = Optional.ofNullable(data.getAirfields())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(this::buildBaseId)
                .map(airfieldDAO::load)
                .collect(Collectors.toList());

        airfields.forEach(airfield -> airfield.addRegion(this));


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
    public Region setRequirements(final List<Squadron> squadrons) {
        // Get the nation's total steps.
        int totalSteps = squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();

        min = determineValue(totalSteps, minString);  //Set the minimum steps that must be deployed in this region.
        max = determineValue(totalSteps, maxString);  //Set the maximum steps that may be deployed in this region.
        return this;
    }

    /**
     * Determine if this region has a minimum squadron deployment requirement.
     *
     * @return True if this region has a minimum squadron deployment requirement. False otherwise.
     */
    public boolean hasMinimumRequirement() {
        return min > 0;
    }

    /**
     * Determine if this region's minimum squadron deployment requirement requires that additional
     * squadrons be deployed.
     *
     * @return The number of steps needed to fulfill this region's minimum squadron deployment requirement.
     */
    public int getNeeded() {
        int neededSteps = min - getCurrentSteps();

        if (neededSteps > 0) {
            return neededSteps;
        }

        return 0;
    }

    /**
     * Determine if this region has roon to add a squadron.
     *
     * @param squadron The potential squadron to add.
     * @return True if the squadron may be added. False otherise.
     */
    public boolean hasRoom(final Squadron squadron) {

        if (max == 0) {
            log.debug("Region: '{}' has room result: true.", name);
            return true; //If the max is zero, then this region has no maximum.
        }

        int steps = squadron.getSteps().intValue();

        boolean result = steps + getCurrentSteps() <= max;

        log.debug("Region: '{}' has room result: {}", name, result);

        if (!result) {
            log.warn("Region: '{}' has max: {} with current: {}", new Object[]{name, max, getCurrentSteps()});
        }


        return result;
    }

    /**
     * Determine if this region's minimum squadron requirement is met.
     *
     * @return True if this region's minimum squadron requirement is met. False if this region's mininum
     * squadron requirement is not yet met.
     */
    public boolean minimumSatisfied() {
        return getNeeded() == 0;
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
     * @param value The min or max step requirement for this region for a given nation.
     * @return The min or max step requirement.
     */
    private int determineValue(final int totalSteps, final String value) {
        if ("HALF".equalsIgnoreCase(value)) {
            return totalSteps / 2;
        }

        return Integer.parseInt(value);
    }

    /**
     * Determine the current number of steps deployed in this region.
     *
     * @return The current number of steps deployed in this region.
     */
    private int getCurrentSteps() {
        return airfields
                .stream()
                .flatMap(airfield -> airfield.getSquadrons().stream())
                .filter(squadron -> nations.contains(squadron.getNation()))
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

}
