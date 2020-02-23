package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.AirfieldType;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.ship.data.GunData;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an squadrons carrier.
 */
public class AircraftCarrier implements Ship, Airbase {

    @Getter
    private final ShipId shipId;

    @Getter
    private final ShipType type;

    @Getter
    private final String shipClass;

    @Getter
    private final AirfieldType airfieldType = AirfieldType.TASKFORCE;

    @Getter
    private final Nation nation;

    @Getter
    private final int victoryPoints;

    @Getter
    @Setter
    private TaskForce taskForce;

    @Getter
    private Gun primary;

    @Getter
    private Gun secondary;

    @Getter
    private Gun tertiary;

    @Getter
    private Gun antiAir;

    @Getter
    private Torpedo torpedo;

    @Getter
    private Asw asw;

    @Getter
    private Movement movement;

    @Getter
    private Fuel fuel;

    @Getter
    private Hull hull;

    @Getter
    private Cargo cargo;

    @Getter
    private String originPort;

    @Getter
    private FlightDeck flightDeck;

    @Getter
    private AircraftCapacity aircraftCapacity;

    @Getter
    private List<LandingType> landingType;

    @Getter
    private List<Squadron> squadrons;

    private Map<String, Squadron> squadronNameMap = new HashMap<>();

    private final Map<AircraftType, List<Squadron>> squadronMap = new LinkedHashMap<>();

    @Getter
    private Map<AircraftType, List<Squadron>> aircraftTypeMap;

    @Getter
    private List<Mission> missions;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     * @param factory Squadron factory that makes the squadrons carrier's squadrons.
     */
    @Inject
    public AircraftCarrier(@Assisted final ShipData data,
                                     final SquadronFactory factory) {

        shipId = data.getShipId();
        type = data.getType();
        shipClass = data.getShipClass();
        nation = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        primary = buildGun("Primary", data.getPrimary());
        secondary = buildGun("Secondary", data.getSecondary());
        tertiary = buildGun("Tertiary", data.getTertiary());
        antiAir = buildGun("Anti-Air", data.getAntiAir());
        torpedo = new Torpedo(data.getTorpedo());
        asw = new Asw(data.getAsw());

        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull());
        cargo = new Cargo(data.getCargo());

        originPort = data.getOriginPort();

        flightDeck = new FlightDeck(data.getFlightDeck());
        aircraftCapacity = buildAircraftCapacity(flightDeck);

        landingType = Optional.ofNullable(data.getLandingType())
                        .orElseGet(Collections::emptyList);

        // Initialize the squadron list for each type of aircraft.
        Stream
                .of(AircraftType.values())
                .sorted()
                .forEach(aircraftType -> squadronMap.put(aircraftType, new ArrayList<>()));

        buildSquadrons(data.getAircraft(), factory);
    }

    /**
     * Build a gun.
     *
     * @param name The name of the gun.
     * @param data The gun's data.
     * @return The gun.
     */
    private Gun buildGun(final String name, final GunData data) {
        data.setName(name);
        return new Gun(data);
    }

    /**
     * Get the ship's persistent data.
     *
     * @return The ship's persistent data.
     */
    @Override
    public ShipData getData() {
        ShipData data = new ShipData();
        data.setShipId(shipId);
        data.setType(type);
        data.setShipClass(shipClass);
        data.setNationality(nation);
        data.setVictoryPoints(victoryPoints);

        data.setSecondary(secondary.getData());
        data.setTertiary(tertiary.getData());
        data.setAntiAir(antiAir.getData());
        data.setTorpedo(torpedo.getData());
        data.setAsw(asw.getData());

        data.setMovement(movement.getData());
        data.setFuel(fuel.getData());
        data.setHull(hull.getData());
        data.setCargo(cargo.getData());

        data.setOriginPort(originPort);

        data.setFlightDeck(flightDeck.getData());

        data.setAircraft(PersistentUtility.getData(squadrons));

        data.setLandingType(landingType);

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
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    @Override
    public List<Component> getComponents() {
        return Stream.of(hull, flightDeck, aircraftCapacity, secondary, tertiary, antiAir, torpedo, movement, fuel, cargo)
                .filter(Component::isPresent)
                .collect(Collectors.toList());
    }

    /**
     * Get the squadrons carriers maximum squadron capacity in steps.
     *
     * @return The carrier's maximum squadron capacity in steps.
     */
    @Override
    public int getMaxCapacity() {
        return aircraftCapacity.getMaxHealth();
    }

    /**
     * Get The squadrons carrier's current squadrons capacity.
     *
     * @return The current squadrons capacity in steps.
     */
    @Override
    public int getCapacity() {
        return aircraftCapacity.getHealth();
    }

    /**
     * Get the current number of steps deployed at this air base.
     *
     * @return The current number of steps deployed at this air base.
     */
    @Override
    public BigDecimal getCurrentSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Determine if the airbase is at capacity, meaning the maximum
     * number of squadron steps that may be stationed at the airbase
     * are stationed at the airbase.
     *
     * @return True if this airbase contains its maximum number of squadron steps.
     */
    @Override
    public boolean isAtCapacity() {
        return getCapacity() == getCurrentSteps().intValue();
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
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation squadronNation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of squadrons for the given nation and given state.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return A list of squadron for the given nation and given state.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation squadronNation, final SquadronState state) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(squadronNation))
                .filter(squadron -> squadron.getSquadronState() == state)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of squadrons for the given nation that can perform the given patrol type.
     *
     * @param squadronNation     The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     * @return A list of squadrons for the given nation that can perform the given patrol.
     */
    @Override
    public List<Squadron> getReadySquadrons(final Nation squadronNation, final PatrolType patrolType) {
        return getSquadrons(squadronNation)
                .stream()
                .filter(squadron -> squadron.canDoPatrol(patrolType))
                .filter(Squadron::isReady)
                .collect(Collectors.toList());    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @param state  The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation squadronNation, final SquadronState state) {
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
     * Get the ship's side: ALLIES or AXIS.
     *
     * @return The ship's side.
     */
    @Override
    public Side getSide() {
        return shipId.getSide();
    }

    /**
     * Get the air base's nations.
     *
     * @return The air base's nations.
     */
    @Override
    public Set<Nation> getNations() {
        return new HashSet<>(Collections.singletonList(nation));
    }

    /**
     * Get the given nations region.
     *
     * @param squadronNation The nation.
     * @return The region that corresponds to the given nation.
     */
    @Override
    public Region getRegion(final Nation squadronNation) {
        return null; // Aircarft carrier's do not have regions.
    }

    /**
     * Get the ship's name.
     *
     * @return The ship's name.
     */
    @Override
    public String getName() {
        return shipId.getName();
    }

    /**
     * Get the map reference of the base.
     *
     * @return The map reference of the base.
     */
    @Override
    public String getReference() {
        return taskForce.getReference();
    }

    /**
     * Get the ship's title. Some ships have revisions or configurations in their name.
     * The getName routine returns this extra information. The get title routine only
     * returns the ship's name/title.
     *
     * @return The ship's title.
     */
    @Override
    public String getTitle() {
        return shipId.getName().replaceAll("-\\d*$", "");
    }

    /**
     * Determines if this ship is an squadrons carrier.
     *
     * @return True if this ship is an squadrons carrier. False otherwise.
     */
    @Override
    public boolean isCarrier() {
        return true;
    }

    /**
     * Determines if this ship has any squadrons carrier based or float planes.
     *
     * @return True if this ship has squadrons. False otherwise.
     */
    @Override
    public boolean hasAircraft() {
        return !squadrons.isEmpty();
    }

    /**
     * Call this method to inform the ship that it is sailing from port.
     */
    @Override
    public void setSail() {
        originPort = taskForce.getReference();
    }

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    @Override
    public void loadCargo() {
        cargo.load();
    }

    /**
     * Get a summary map of squadrons type to number of steps of that type.
     *
     * @return A map of squadrons types to number of steps of that type.
     */
    @Override
    public Map<AircraftType, BigDecimal> getSquadronSummary() {
        return aircraftTypeMap
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                                          e -> sumSteps(e.getValue()),
                                          BigDecimal::add));
    }

    /**
     * Base a squadron from this airfield.
     *
     * @param squadron The squadron which is now based at this airfield.
     */
    @Override
    public AirfieldOperation addSquadron(final Squadron squadron) {
        Optional.ofNullable(squadron.getAirfield())
                .ifPresent(airfield -> airfield.removeSquadron(squadron));

        AirfieldOperation result = canStation(squadron);

        if (result == AirfieldOperation.SUCCESS) {
            squadrons.add(squadron);

            squadron.setAirfield(this);

            squadronMap
                    .get(squadron.getType())
                    .add(squadron);

            squadronNameMap.put(squadron.getName(), squadron);
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

        squadron.setAirfield(null);
    }

    /**
     * Get the current missions of this air base.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the current missions.
     */
    @Override
    public List<Mission> getMissions(final Nation squadronNation) {
        return missions
                .stream()
                .filter(mission -> mission.getNation() == nation)
                .collect(Collectors.toList());
    }

    /**
     * Add a mission to this air base.
     *
     * @param mission The mission that is added to this airbase.
     */
    @Override
    public void addMission(final Mission mission) {

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
        return missions
                .stream()
                .filter(mission -> mission.getTarget().isEqual(target))
                .map(Mission::getSteps)
                .reduce(0, Integer::sum);
    }

    /**
     * Get the Patrol specified by the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The Patrol that corresponds to the given patrol type.
     */
    @Override
    public Patrol getPatrol(final PatrolType patrolType) {
        return null;
    }

    /**
     * This is a utility function to aid in determining patrol stats for squadrons that are
     * selected for a given patrol type but not necessarily committed to the patrol yet.
     *
     * @param patrolType       The type of patrol.
     * @param squadronOnPatrol A list of potential squadrons on patrol.
     * @return A patrol consisting of the given squadrons.
     */
    @Override
    public Patrol getTemporaryPatrol(final PatrolType patrolType, final List<Squadron> squadronOnPatrol) {
        return null;
    }

    /**
     * Clear all of the patrols and missions on this airbase.
     */
    @Override
    public void clearPatrolsAndMissions() {

    }

    /**
     * Get the air base's anti aircraft rating.
     *
     * @return The air base's anti aircraft rating.
     */
    @Override
    public int getAntiAirRating() {
        return antiAir.getHealth();
    }

    /**
     * Get the strength in steps of the given list of squadrons.
     *
     * @param squads A list of squadrons of a given squadrons type.
     * @return The total strength of the list of squadrons.
     */
    private BigDecimal sumSteps(final List<Squadron> squads) {
        return squads
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Build the squadrons capacity.
     *
     * @param deck The squadrons carrier's flight deck.
     * @return The ship's squadrons carrier capacity.
     */
    private AircraftCapacity buildAircraftCapacity(final FlightDeck deck) {
        AircraftCapacity capacity = new AircraftCapacity();
        capacity.setHealth(deck.getCapacity());
        capacity.setMaxHealth(deck.getMaxCapacity());
        return capacity;
    }

    /**
     * Build the ship squadrons. Do not examine the landing type. Some
     * scenario's require that carriers be loaded with squadrons that
     * can take off but not land. Thus, we ignore the landing type
     * on initial ship creation.
     *
     * @param data The squadrons data read in from a JSON file.
     * @param factory The squadron factory that builds the actual squadron.
     */
    private void buildSquadrons(final List<SquadronData> data, final SquadronFactory factory) {
        squadrons = Optional.ofNullable(data)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronData -> factory.create(shipId.getSide(), nation, squadronData))
                .collect(Collectors.toList());

        aircraftTypeMap = squadrons
                .stream()
                .collect(Collectors.groupingBy(Squadron::getType));
    }

    /**
     * Determine if this airfield has room for another squadron.
     *
     * @param squadron The new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    public AirfieldOperation canStation(final Squadron squadron) {
        if (!landingType.contains(squadron.getLandingType())) {
            return AirfieldOperation.LANDING_TYPE_NOT_SUPPORTED;
        }

        return hasRoom(squadron) ? AirfieldOperation.SUCCESS : AirfieldOperation.BASE_FULL;
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
     * Determine if the squadrons carrier has room for the given squadron.
     *
     * @param squadron A squadron that may be based at this squadrons carrier.
     * @return True if this squadrons carrier has room for the given squadron. False otherwise.
     */
    private boolean hasRoom(final Squadron squadron) {
        int steps = squadron.getSteps().intValue();
        return steps + deployedSteps() <= getMaxCapacity();
    }
}
