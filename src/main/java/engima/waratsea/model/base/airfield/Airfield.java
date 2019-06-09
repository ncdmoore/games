package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents airfield's in the game.
 */
@Slf4j
public class Airfield implements Asset, Airbase, PersistentData<AirfieldData> {

    @Getter
    private final Side side;

    @Getter
    private final String name;

    @Getter
    private final int maxCapacity;   //Capacity in steps.

    @Getter
    private final int antiAir;

    @Getter
    private final String reference; // A simple string is used to prevent circular logic on mapping names and references.
                                    // Airfields are used to map airfield names to map references. Thus, we just need a map reference
    @Getter
    @Setter
    private int capacity;           //Capacity in steps.

    private Map<Nation, Region> regions = new HashMap<>();

    @Getter
    private List<Squadron> squadrons = new ArrayList<>();

    /**
     * Constructor called by guice.
     *
     * @param data The airfield data read in from a JSON file.
     */
    @Inject
    public Airfield(@Assisted final AirfieldData data) {
        this.side = data.getSide();
        name = data.getName();
        maxCapacity = data.getMaxCapacity();
        capacity = maxCapacity;
        antiAir = data.getAntiAir();
        reference = data.getLocation();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public AirfieldData getData() {
        AirfieldData data = new AirfieldData();
        data.setSide(side);
        data.setName(name);
        data.setMaxCapacity(maxCapacity);
        data.setCapacity(capacity);
        data.setAntiAir(antiAir);
        data.setLocation(reference);
        return data;
    }

    /**
     * Add a region to this airfield.
     *
     * @param region The region that is added.
     */
    public void addRegion(final Region region) {
        region.getNations().forEach(nation -> regions.put(nation, region));
    }

    /**
     * Get the given nations region.
     *
     * @param nation The nation.
     * @return The region that corresponds to the given nation.
     */
    public Region getRegion(final Nation nation) {
        return regions.get(nation);
    }

    /**
     * Determine if this airfield may be used by the given nation.
     *
     * @param nation The nation.
     * @return True if the given nation may base squadrons at this airfield. False otherwise.
     */
    public boolean usedByNation(final Nation nation) {
        return regions.containsKey(nation);
    }

    /**
     * Get a set of the airfield's nations.
     *
     * @return A set of the airfield's nations.
     */
    public Set<Nation> getNations() {
        return regions.keySet();
    }

    /**
     * Base a squadron from this airfield.
     *
     * @param squadron The squadron which is now based at this airfield.
     */
    @Override
    public boolean addSquadron(final Squadron squadron) {

        if (hasRoom(squadron)) {
            Optional.ofNullable(squadron.getAirfield())
                    .ifPresent(airfield -> airfield.removeSquadron(squadron));
            squadrons.add(squadron);
            squadron.setAirfield(this);
            return true;
        }

        return false;
    }

    /**
     * Remove a squadron from this airfield.
     *
     * @param squadron The squadron which is removed from this airfield.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
        squadron.setAirfield(null);
    }

    /**
     * Remove all of the given nation's squadrons.
     *
     * @param nation The nation BRITISH, ITALIAN, etc...
     * @return The airfield.
     */
    public Airfield removeAllSquadrons(final Nation nation) {
        List<Squadron> toBeRemoved = squadrons
                .stream()
                .filter(squadron -> squadron.getNation() == nation)
                .collect(Collectors.toList());

        squadrons.removeAll(toBeRemoved);

        return this;
    }

    /**
     * Get the current number of steps.
     *
     * @return The current number of steps deployed at this airfield.
     */
    public BigDecimal getCurrentSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get a map of steps by aircraft base type.
     *
     * @param type An aircraft base type.
     * @return The number of steps of aircraft of the given type based at this airfield.
     */
    public BigDecimal getStepsForType(final AircraftBaseType type) {
        return  squadrons
                .stream()
                .filter(squadron -> squadron.getBaseType() == type)
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * The String representation of the airfield.
     *
     * @return The String representation of the airfield.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Determine if this airfield has room for another squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    private boolean hasRoom(final Squadron squadron) {

        Nation nation = squadron.getNation();
        Region region = regions.get(nation);

        return determineRoom(squadron) && region.hasRoom(squadron);
    }

    /**
     * Determine if this airfield has room for the given squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this arifield can house the new squadron. False otherwise.
     */
    private boolean determineRoom(final Squadron squadron) {
        int steps = squadron.getSteps().intValue();

        boolean result = steps + deployedSteps() <= maxCapacity;

        log.debug("Airfield: '{}' has room result: {}", name, result);

        if (!result) {
            log.warn("Airfield: '{}' has max capacity: {} and current capacity: {}", new Object[]{name, maxCapacity, deployedSteps()});
        }

        return result;
    }

    /**
     * Determine the current number of steps deployed at this airfield.
     *
     * @return The current number of steps deployed at this airfield.
     */
    private int deployedSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }

    /**
     * The map location of the asset.
     *
     * @return The map location of the asset.
     */
    @Override
    public String getLocation() {
        return reference;
    }

    /**
     * Get the active state of the asset.
     *
     * @return True if the asset is active. False if the asset is not active.
     */
    @Override
    public boolean isActive() {
        return true;
    }
}
