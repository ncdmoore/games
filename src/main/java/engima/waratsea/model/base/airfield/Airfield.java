package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.patrol.AswPatrol;
import engima.waratsea.model.base.airfield.patrol.CapPatrol;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.SearchPatrol;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final String title;

    @Getter
    private final List<LandingType> landingType;

    @Getter
    private final AirfieldType airfieldType;

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

    private Map<AircraftType, List<Squadron>> squadronMap = new LinkedHashMap<>();

    private Map<PatrolType, Patrol> patrolMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param data The airfield data read in from a JSON file.
     * @param factory Squadron factory.
     */
    @Inject
    public Airfield(@Assisted final AirfieldData data,
                              final SquadronFactory factory) {
        this.side = data.getSide();
        name = data.getName();
        title = Optional.ofNullable(data.getTitle()).orElse(name);   // If no title is specified just use the name.
        landingType = data.getLandingType();
        airfieldType = determineType();
        maxCapacity = data.getMaxCapacity();
        capacity = maxCapacity;
        antiAir = data.getAntiAir();
        reference = data.getLocation();

        // Initialize the squadron list for each type of aircraft.
        Stream
                .of(AircraftType.values())
                .sorted()
                .forEach(type -> squadronMap.put(type, new ArrayList<>()));

        buildSquadrons(data.getSquadrons(), factory);
        buildPatrols(data);
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
        data.setTitle(title);
        data.setLandingType(landingType);
        data.setMaxCapacity(maxCapacity);
        data.setCapacity(capacity);
        data.setAntiAir(antiAir);
        data.setLocation(reference);

        data.setSquadrons(PersistentUtility.getData(squadrons));
        data.setAswPatrol(patrolMap.get(PatrolType.ASW).getData());
        data.setCapPatrol(patrolMap.get(PatrolType.CAP).getData());
        data.setSearchPatrol(patrolMap.get(PatrolType.SEARCH).getData());

        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
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
     * Indicates if this airbase has any squadrons.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    @Override
    public boolean areSquadronsPresent() {
        return !squadrons.isEmpty();
    }

    /**
     * Base a squadron from this airfield.
     *
     * @param squadron The squadron which is now based at this airfield.
     */
    @Override
    public AirfieldOperation addSquadron(final Squadron squadron) {

        AirfieldOperation result = canStation(squadron);

        if (result == AirfieldOperation.SUCCESS) {
            Optional.ofNullable(squadron.getAirfield())
                    .ifPresent(airfield -> airfield.removeSquadron(squadron));
            stationSquadron(squadron);
        }

        return result;
    }

    /**
     * Remove a squadron from this airfield.
     *
     * @param squadron The squadron which is removed from this airfield.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);

        squadronMap
                .get(squadron.getType())
                .remove(squadron);

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
     * Get the list of squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    public List<Squadron> getSquadrons(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of squadrons for the given nation and given state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return A list of squadron for the given nation and given state.
     */
    public List<Squadron> getSquadrons(final Nation nation, final SquadronState state) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .filter(squadron -> squadron.getSquadronState() == state)
                .collect(Collectors.toList());
    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation nation, final SquadronState state) {
        return squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          entry -> entry
                                                    .getValue()
                                                    .stream()
                                                    .filter(squadron -> squadron.ofNation(nation))
                                                    .filter(squadron -> squadron.getSquadronState() == state)
                                                    .collect(Collectors.toList()),
                                          (oldList, newList) -> oldList,
                                          LinkedHashMap::new));
    }

    /**
     * Get the number of steps stationed at this airfield for the
     * given base type of aircraft.
     *
     * @param type An aircraft base type.
     * @return The number of steps of aircraft of the given type based at this airfield.
     */
    public BigDecimal getStepsForType(final AircraftBaseType type) {
        return squadrons
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
        return title;
    }


    /**
     * Determine if the given squadron can land at this airfield.
     *
     * @param squadron The squadron that is checked to determine if it can land at this airfield.
     * @return True if the given squadron may land at this airfield. Otherwise, false.
     */
    public boolean canSquadronLand(final Squadron squadron) {
        return landingType.contains(squadron.getLandingType());
    }


    /**
     * Get a list of squadrons for the given nation that can perform the given patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     * @return A list of squadrons for the given nation that can perform the given patrol.
     */
    public List<Squadron> getReadySquadrons(final Nation nation, final PatrolType patrolType) {
        return getSquadrons(nation)
                .stream()
                .filter(squadron -> squadron.canDoPatrol(patrolType))
                .filter(Squadron::isReady)
                .collect(Collectors.toList());
    }

    /**
     * Get a patrol given the patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The patrol corresponding to the given type.
     */
    public Patrol getPatrol(final PatrolType patrolType) {
        return patrolMap.get(patrolType);
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

    /**
     * Build the airfield's squadrons. This is only valid for saved games where the airfield
     * squadrons are already known.
     *
     * @param data A List of the squadron data.
     * @param factory The squadron factory.
     */
    private void buildSquadrons(final List<SquadronData> data, final SquadronFactory factory) {
        Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(side, squadronData.getNation(), squadronData))
                .forEach(this::stationSquadron);
    }

    /**
     * Build the airfield's ASW patrol.
     *
     * @param data The Airfield data read in from a JSON file.
     */
    private void buildPatrols(final AirfieldData data) {
        PatrolData aswData = data.getAswPatrol();

        aswData.setAirbase(this);
        patrolMap.put(PatrolType.ASW, new AswPatrol(aswData));

        PatrolData capData = data.getCapPatrol();

        capData.setAirbase(this);
        patrolMap.put(PatrolType.CAP, new CapPatrol(capData));

        PatrolData searchData = data.getSearchPatrol();

        searchData.setAirbase(this);
        patrolMap.put(PatrolType.SEARCH, new SearchPatrol(searchData));
    }

    /**
     * Determine if this airfield can station another squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    private AirfieldOperation canStation(final Squadron squadron) {

        if (!canSquadronLand(squadron)) {
            return AirfieldOperation.LANDING_TYPE_NOT_SUPPORTED;
        }

        Nation nation = squadron.getNation();
        Region region = regions.get(nation);

        if (!region.hasRoom(squadron)) {
            return AirfieldOperation.REGION_FULL;
        }

        if (!determineRoom(squadron)) {
            return AirfieldOperation.BASE_FULL;
        }

        return AirfieldOperation.SUCCESS;
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
     * Station a squadron at this airfield.
     *
     * @param squadron The squadron that is stationed.
     */
    private void stationSquadron(final Squadron squadron) {
        squadrons.add(squadron);
        squadron.setAirfield(this);

        squadronMap
                .get(squadron.getType())
                .add(squadron);
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
     * Determine the type of airfield based on the types of squadrons that can land.
     *
     * @return The type of airfield.
     */
    private AirfieldType determineType() {
        if (landingType.contains(LandingType.LAND) && landingType.contains(LandingType.SEAPLANE)) {
            return AirfieldType.BOTH;
        }

        return landingType.contains(LandingType.LAND) ? AirfieldType.LAND : AirfieldType.SEAPLANE;
    }
}
