package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.Missions;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.Patrols;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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
    @Getter private final Side side;
    @Getter private final String name;
    @Getter private final String title;
    @Getter private final List<LandingType> landingType;
    @Getter private final AirfieldType airfieldType;
    @Getter private final int maxCapacity;          // Capacity in steps.
    @Getter private final int antiAirRating;
    @Getter private final String reference;         // A simple string is used to prevent circular logic on mapping names and references.
                                                    // Airfields are used to map airfield names to map references. Thus, we just need a map reference
    @Getter @Setter private int capacity;           // Capacity in steps.

    @Getter private final List<Squadron> squadrons = new ArrayList<>();

    private final Map<Nation, Region> regions = new HashMap<>();  // A given airfield may be in multiple regions.
    private final Map<String, Squadron> squadronNameMap = new HashMap<>();
    private final Map<AircraftType, List<Squadron>> squadronMap = new LinkedHashMap<>();

    private final Missions missions;
    private final Patrols patrols;

    /**
     * Constructor called by guice.
     *
     * @param data The airfield data read in from a JSON file.
     * @param squadronFactory Squadron factory.
     * @param missions  This airbase's missions.
     * @param patrols This airbase's patrols.
     */
    @Inject
    public Airfield(@Assisted final AirfieldData data,
                    final SquadronFactory squadronFactory,
                    final Missions missions,
                    final Patrols patrols) {

        this.missions = missions;
        this.patrols = patrols;

        this.side = data.getSide();
        name = data.getName();
        title = Optional.ofNullable(data.getTitle()).orElse(name);   // If no title is specified just use the name.
        landingType = data.getLandingType();
        airfieldType = determineType();
        maxCapacity = data.getMaxCapacity();
        capacity = maxCapacity;
        antiAirRating = data.getAntiAir();
        reference = data.getLocation();

        // Initialize the squadron list for each type of aircraft.
        Stream
                .of(AircraftType.values())
                .sorted()
                .forEach(type -> squadronMap.put(type, new ArrayList<>()));

        buildSquadrons(data.getSquadrons(), squadronFactory);

        missions.build(this, data.getMissionsData());
        patrols.build(this, data.getPatrolsData());
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
        data.setAntiAir(antiAirRating);
        data.setLocation(reference);

        data.setSquadrons(PersistentUtility.getData(squadrons));

        data.setMissionsData(missions.getData());
        data.setPatrolsData(patrols.getData());

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
        regions.put(region.getNation(), region);
    }

    /**
     * Get the given nations region.
     *
     * @param nation The nation.
     * @return The region that corresponds to the given nation.
     */
    @Override
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
    @Override
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

        squadronNameMap.remove(squadron.getName());

        squadron.setHome(null);
    }

    /**
     * Get the airfield's missions for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of missions for the given nation.
     */
    @Override
    public List<AirMission> getMissions(final Nation nation) {
        return missions.getMissions(nation);
    }

    /**
     * Add a mission to this air base.
     *
     * @param mission The mission that is added to this airbase.
     */
    @Override
    public void addMission(final AirMission mission) {
        missions.addMission(mission);
    }

    /**
     * Get the total number of squadron steps on a mission of the given type
     * that are assigned to the given target. This is the total number of squadron steps
     * from all missions of the same type that have the given target as their target.
     *
     * @param target The ferry mission destination.
     * @return The total number of steps being ferried to the given target.
     */
    @Override
    public int getTotalMissionSteps(final Target target) {
        return missions.getTotalMissionSteps(target);
    }

    /**
     * Clear all of the patrols and missions on this airbase.
     */
    @Override
    public void clearPatrolsAndMissions() {
        patrols.clear();
        missions.clear();
    }

    /**
     * Remove all of the given nation's squadrons.
     *
     * @param nation The nation BRITISH, ITALIAN, etc...
     * @return The airfield.
     */
    public Airfield removeAllSquadrons(final Nation nation) {
        missions.clear(nation);
        patrols.clear(nation);
        List<Squadron> toBeRemoved = getSquadrons(nation);
        squadrons.removeAll(toBeRemoved);

        toBeRemoved
                .stream()
                .peek(squadron -> squadronMap.get(squadron.getType()).remove(squadron))
                .map(Squadron::getName)
                .forEach(squadronNameMap::remove);

        return this;
    }

    /**
     * Get the current number of steps.
     *
     * @return The current number of steps deployed at this airfield.
     */
    @Override
    public BigDecimal getCurrentSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get the squadron given its name.
     *
     * @param squadronName The squadron name.
     * @return The squadron that corresponds to the given squadron name.
     */
    @Override
    public Squadron getSquadron(final String squadronName) {
        return squadronNameMap.get(squadronName);
    }

    /**
     * Get the list of squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    @Override
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
    @Override
    public List<Squadron> getSquadrons(final Nation nation, final SquadronState state) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .filter(squadron -> squadron.isAtState(state))
                .collect(Collectors.toList());
    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation nation) {
        return squadronMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry
                                .getValue()
                                .stream()
                                .filter(squadron -> squadron.ofNation(nation))
                                .collect(Collectors.toList()),
                        (oldList, newList) -> oldList,
                        LinkedHashMap::new));
    }
    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation nation, final SquadronState state) {

        return getSquadronMap(nation)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry
                        .getValue()
                        .stream()
                        .filter(squadron -> squadron.isAtState(state))
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
    @Override
    public List<Squadron> getReadySquadrons(final Nation nation, final PatrolType patrolType) {
        return getSquadrons(nation)
                .stream()
                .filter(squadron -> squadron.canDoPatrol(patrolType))
                .filter(squadron -> squadron.isAtState(SquadronState.READY))
                .collect(Collectors.toList());
    }

    /**
     * Get a map of nation to list of squadrons.
     *
     * @return A map of nation to list of squadrons.
     */
    @Override
    public Map<Nation, List<Squadron>> getSquadronMap() {
        return getNations()
                .stream()
                .collect(Collectors.toMap(nation -> nation, this::getSquadrons));
    }

    /**
     * Get a patrol given the patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The patrol corresponding to the given type.
     */
    @Override
    public Patrol getPatrol(final PatrolType patrolType) {
        return patrols.getPatrol(patrolType);
    }

    /**
     * Get a map of patrol maximum radius to list of patrols.
     *
     * @return A map containing the patrol maximum radius as key and the patrol as the value.
     * Note, that if all the patrol types have the same maximum radius value then this map
     * will contain a single entry where the single maximum radius maps to all three types
     * of patrols.
     */
    public Map<Integer, List<Patrol>> getPatrolRadiiMap() {
        return patrols.getPatrolRadiiMap();
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
     * Determine if this airfield can station another squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    @Override
    public AirfieldOperation canStation(final Squadron squadron) {

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
     * @return True if this airfield can house the new squadron. False otherwise.
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
        squadron.setHome(this);

        squadronMap
                .get(squadron.getType())
                .add(squadron);

        squadronNameMap.put(squadron.getName(), squadron);
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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure
     * {@code sgn(x.compareTo(y)) == -sgn(y.compareTo(x))}
     * for all {@code x} and {@code y}.  (This
     * implies that {@code x.compareTo(y)} must throw an exception iff
     * {@code y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code x.compareTo(y)==0}
     * implies that {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for
     * all {@code z}.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
     * <i>signum</i> function, which is defined to return one of {@code -1},
     * {@code 0}, or {@code 1} according to whether the value of
     * <i>expression</i> is negative, zero, or positive, respectively.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull final Base o) {
        return getTitle().compareTo(o.getTitle());
    }
}
