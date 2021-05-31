package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.AirOperations;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.Missions;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.Patrols;
import engima.waratsea.model.base.airfield.squadron.Squadrons;
import engima.waratsea.model.base.airfield.squadron.data.SquadronsData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.SeaRegion;
import engima.waratsea.model.ship.data.GunData;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a capital ship: Battleship, cruisers, etc.
 *
 * These ships may contain float planes. They may conduct air operations. Thus, they implement the airbase interface.
 *
 * This class may be viewed as either a ship or an airbase since it implements both interfaces.
 */
public class CapitalShip implements Ship, Airbase {
    @Getter private final ShipId shipId;
    @Getter private final ShipType type;
    @Getter private final String shipClass;
    @Getter private final Nation nation;
    @Getter private final int victoryPoints;
    @Getter @Setter private TaskForce taskForce;
    @Getter private final Gun primary;
    @Getter private final Gun secondary;
    @Getter private final Gun tertiary;
    @Getter private final Gun antiAir;
    @Getter private final Torpedo torpedo;
    @Getter private final Asw asw;
    @Getter private final Movement movement;
    @Getter private final Fuel fuel;
    @Getter private final Hull hull;
    @Getter private final Cargo cargo;
    @Getter private String originPort;

    @Getter @Setter private AmmunitionType ammunitionType;

    @Getter private final AirbaseType airbaseType = AirbaseType.FlOAT_PLANE;

    @Getter private final List<LandingType> landingType;

    private final Squadrons squadrons;

    private final Missions missions;
    private final Patrols patrols;
    private final AirOperations airOperations;
    private final Region region;

    /**
     * Constructor called by guice.
     *
     * @param data Ship's data.
     * @param squadrons The aircraft carrier's squadrons.
     * @param missions The aircraft carriers air missions.
     * @param patrols The aircraft carriers air patrols.
     * @param airOperations This carrier's air operations.
     * @param region The ship's region.
     */
    @Inject
    public CapitalShip(@Assisted final ShipData data,
                       final Squadrons squadrons,
                       final Missions missions,
                       final Patrols patrols,
                       final AirOperations airOperations,
                       final SeaRegion region) {

        this.squadrons = squadrons;
        this.missions = missions;
        this.patrols = patrols;
        this.airOperations = airOperations;
        this.region = region;

        shipId = data.getShipId();
        taskForce = data.getTaskForce();
        type = data.getType();
        shipClass = data.getShipClass();
        nation = data.getNationality();
        victoryPoints = data.getVictoryPoints();

        primary = buildGun("Primary", data.getPrimary());
        secondary = buildGun("Secondary", data.getSecondary());
        tertiary = buildGun("Tertiary", data.getTertiary());
        antiAir = buildGun("Anti-Air", data.getAntiAir());
        ammunitionType = Optional.ofNullable(data.getAmmunitionType()).orElse(AmmunitionType.NORMAL);
        torpedo = new Torpedo(data.getTorpedo());
        asw = new Asw(data.getAsw());

        movement = new Movement(data.getMovement());
        fuel = new Fuel(data.getFuel());
        hull = new Hull(data.getHull());
        cargo = new Cargo((data.getCargo()));

        originPort = data.getOriginPort();

        landingType = List.of(LandingType.FLOATPLANE);

        Optional.ofNullable(data.getSquadronsData())
                .map(SquadronsData::getSquadrons)
                .ifPresent(squadronDataList -> squadronDataList.forEach(s -> s.setNation(nation)));

        squadrons.build(this, data.getSquadronsData());
        missions.build(this, data.getMissionsData());
        patrols.build(this, data.getPatrolsData());
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

        data.setPrimary(primary.getData());
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

        data.setAmmunitionType(ammunitionType);

        data.setSquadronsData(squadrons.getData());
        data.setMissionsData(missions.getData());
        data.setPatrolsData(patrols.getData());

        return data;
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
     * Get the ship's name.
     *
     * @return The ship's name.
     */
    @Override
    public String getName() {
        return shipId.getName();
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
        return getName();
    }

    /**
     * Get the airbase's nations. These are the nations that are permitted to station squadrons at this airbase.
     * Not all nations are allowed to station squadrons at all airbase's.
     *
     * @return The nations that allowed to station squadrons at this airbase.
     */
    @Override
    public Set<Nation> getNations() {
        return Set.of(nation);
    }

    /**
     * Determine if the given nation may use or station squadrons at this airbase.
     *
     * @param country The nation: BRITISH, ITALIAN, etc...
     * @return True if the given nation can use this airbase. False otherwise.
     */
    @Override
    public boolean canUse(final Nation country) {
        return false; // Squadrons cannot be ferried to a capital ship.
    }                 // However, squadrons can be ferried from a capital ship.

    /**
     * Get the ship's region for the given nation.
     *
     * @param shipNation The nation: BRITISH or ITALIAN, etc...
     * @return Ships do not have regions, so null is returned.
     */
    @Override
    public Region getRegion(final Nation shipNation) {
        return region;
    }

    /**
     * Get the region's title. The regions title should
     * be independent of the nation.
     *
     * @return The region's title.
     */
    @Override
    public String getRegionTitle() {
        return region.getTitle();
    }

    /**
     * Get the ship's map reference.
     *
     * @return The ship's map reference.
     */
    @Override
    public String getReference() {
        return taskForce.getReference();
    }

    /**
     * Get the squadron's home game grid.
     *
     * @return The squadron's home game grid.
     */
    @Override
    public Optional<GameGrid> getGrid() {
        return taskForce.getGrid();
    }

    /**
     * Determines if this ship is an squadrons carrier.
     *
     * @return True if this ship is an squadrons carrier. False otherwise.
     */
    @Override
    public boolean isAirbase() {
        return true;
    }

    /**
     * The maximum capacity of the airbase. This is the undamaged squadron step capacity of this airbase.
     *
     * @return The maximum squadron step capacity of this airbase.
     */
    @Override
    public int getMaxCapacity() {
        return 2;
    }

    /**
     * The current squadron step capacity of the airbase. A base's capacity to station
     * squadrons may be reduced via air attack or naval bombardment. This method returns the
     * current capacity taking into account any damage or repairs from air attacks or
     * bombardments.
     *
     * @return The current capacity of the airbase in steps.
     */
    @Override
    public int getCapacity() {
        return 2;
    }

    /**
     * Get the current number of squadron steps stationed at this airbase. This is
     * the total number of squadron steps of all squadrons currently stationed at
     * this airbase. This does not take into account any squadron steps that
     * are given a ferry mission to this airbase. The returned value includes
     * squadrons steps for all nations currently using this airbase.
     *
     * @return The current number of steps deployed at this airbase.
     */
    @Override
    public BigDecimal getCurrentSteps() {
        return squadrons.getCurrentSteps();
    }

    /**
     * Indicates if this airbase has any squadrons currently stationed.
     * This includes all nations that may station squadrons at this airbase.
     * This does not take into account any squadrons given a ferry mission
     * to this airbase.
     *
     * @return True if any squadron is based at this airbase. False otherwise.
     */
    @Override
    public boolean areSquadronsPresent() {
        return squadrons.areSquadronsPresent();
    }

    /**
     * Indicates if the given nation has any squadrons currently stationed at
     * this airbase. This does not take into account any squadron given a ferry
     * mission to this airbase.
     *
     * @param targetNation The nation: BRITISH, ITALIAN, etc...
     * @return True if the given nation has squadrons based at this airbase. False otherwise.
     */
    @Override
    public boolean areSquadronsPresent(final Nation targetNation) {
        return squadrons.areSquadronsPresent(targetNation);
    }

    /**
     * Indicates if the given squadron is stationed at this airbase.
     *
     * @param squadron The squadron that is checked to determine if this airbase is its home.
     * @return True if the given squadron is stationed at this airbase. False otherwise.
     */
    @Override
    public boolean isStationed(final Squadron squadron) {
        return squadrons.isStationed(squadron);
    }

    /**
     * Get a squadron stationed at this airbase given its name.
     *
     * @param squadronName The squadron name.
     * @return The squadron that corresponds to the given squadron name.
     */
    @Override
    public Squadron getSquadron(final String squadronName) {
        return squadrons.getSquadron(squadronName);
    }

    /**
     * Get all the squadrons currently stationed at this airbase. This includes all nations.
     *
     * @return The all squadrons currently stationed at this airbase.
     */
    @Override
    public List<Squadron> getSquadrons() {
        return squadrons.getSquadrons();
    }

    /**
     * Get the list of squadrons currently stationed at this airbase for the given nation.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @return The squadrons currently stationed at this airbase for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation squadronNation) {
        return squadrons.getSquadrons(squadronNation);
    }

    /**
     * Get the list of squadrons currently stationed at this airbase for the given nation and given state.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @param state  The squadron state. Only squadrons in this state are included in the squadrons returned.
     * @return The squadrons for the given nation and in the given state.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation squadronNation, final SquadronState state) {
        return squadrons.getSquadrons(squadronNation, state);
    }

    /**
     * Get a map of nation to list of squadrons for that nation.
     *
     * @return A map of nation to list of squadrons.
     */
    @Override
    public Map<Nation, List<Squadron>> getSquadronMap() {
        return getNations()
                .stream()
                .collect(Collectors.toMap(country -> country, this::getSquadrons));
    }

    /**
     * Get the squadron map for the given nation. This is map of
     * aircraft type to a list of squadrons of the type of aircraft.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @return A squadron map keyed by aircraft type to list of squadrons of that type.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation squadronNation) {
        return squadrons.getSquadronMap(squadronNation);
    }

    /**
     * Get the squadron map for the given nation and given squadron state. This is map of
     * aircraft type to list of this type of aircraft type squadrons.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @param state  The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation squadronNation, final SquadronState state) {
        return squadrons.getSquadronMap(squadronNation, state);
    }

    /**
     * Get a list of the aircraft models present at this airbase.
     *
     * @param squadronNation The nation.
     * @return A unique list of aircraft that represent the aircraft models present at this airbase.
     */
    @Override
    public List<Aircraft> getAircraftModelsPresent(final Nation squadronNation) {
        return squadrons.getAircraftModelsPresent(squadronNation);
    }

    /**
     * Add a squadron to this airbase. Station the given squadron at this airbase.
     * Note, if the airbase does not support the squadron landing type or if the
     * airbase does not have capacity to station the squadron, then the squadron will
     * not be stationed.
     *
     * @param squadron The squadron that is now based at this airbase.
     * @return True if the squadron was added. False otherwise.
     */
    @Override
    public AirfieldOperation addSquadron(final Squadron squadron) {
        AirfieldOperation result = canStation(squadron);

        if (result == AirfieldOperation.SUCCESS) {
            squadrons.add(squadron);
        }

        return result;
    }

    /**
     * Remove a squadron from this airbase.
     *
     * @param squadron The squadron that is removed from this airbase.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
    }

    /**
     * Get the current missions of this airbase for all nations.
     *
     * @return The current squadron missions of this airbase.
     */
    @Override
    public List<AirMission> getMissions() {
        return missions.getMissions();
    }

    /**
     * Get the current missions of this airbase for the given nation.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc...
     * @return The current squadron missions of this airbase for the given nation.
     */
    @Override
    public List<AirMission> getMissions(final Nation squadronNation) {
        return missions.getMissions(squadronNation);
    }

    /**
     * Add a mission to this airbase.
     *
     * @param mission The mission that is added to this airbase.
     */
    @Override
    public void addMission(final AirMission mission) {
        missions.addMission(mission);
    }

    /**
     * Get the total number of squadron steps on missions assigned to the given target.
     * This is the total number of squadron steps from all missions that have
     * the given target as their assigned target.
     *
     * @param target The mission target.
     * @return The total number of squadron steps on missions assigned to the given target.
     */
    @Override
    public int getTotalMissionSteps(final Target target) {
        return missions.getTotalMissionSteps(target);
    }

    /**
     * Get this airbase's patrol specified by the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The patrol that corresponds to the given patrol type.
     */
    @Override
    public Patrol getPatrol(final PatrolType patrolType) {
        return patrols.getPatrol(patrolType);
    }

    /**
     * Clear all missions, patrols and squadrons for this airbase.
     */
    @Override
    public void clear() {
        missions.clear();
        patrols.clear();
    }

    /**
     * Update the patrol.
     *
     * @param patrolType The type of patrol.
     * @param squadronsOnPatrol  The squadrons on patrol.
     */
    @Override
    public void updatePatrol(final PatrolType patrolType, final List<Squadron> squadronsOnPatrol) {
        patrols.update(patrolType, squadronsOnPatrol);
    }

    /**
     * Get this airbase's air operation stats.
     *
     * @return This airbase's air operation stats.
     */
    @Override
    public List<ProbabilityStats> getAirOperationStats() {
        return airOperations.getStats(this);
    }

    /**
     * Get the airbase's anti aircraft rating.
     *
     * @return The airbase's anti aircraft rating.
     */
    @Override
    public int getAntiAirRating() {
        return antiAir.getHealth();
    }

    /**
     * Determine if the given squadron can be stationed at this airbase.
     * This does not take into account any squadron steps that are given
     * a ferry mission to this airbase.
     *
     * @param squadron the squadron to station at this airbase.
     * @return The results of the squadron station operation. Success if
     * the squadron can be stationed. Otherwise an error code is returned.
     */
    @Override
    public AirfieldOperation canStation(final Squadron squadron) {
        if (!landingType.contains(squadron.getLandingType())) {
            return AirfieldOperation.LANDING_TYPE_NOT_SUPPORTED;
        }

        return hasRoom(squadron) ? AirfieldOperation.SUCCESS : AirfieldOperation.BASE_FULL;
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
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    @Override
    public List<Component> getComponents() {
        return Stream.of(hull, primary, secondary, tertiary, antiAir, torpedo, movement, fuel, cargo)
                .filter(Component::isPresent)
                .collect(Collectors.toList());
    }

    /**
     * Determine the current number of steps deployed at this airfield.
     *
     * @return The current number of steps deployed at this airfield.
     */
    private int deployedSteps() {
        return squadrons.deployedSteps();
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

    /**
     * Get the String representation of this ship.
     *
     * @return The String representation of this ship.
     */
    @Override
    public String toString() {
        return getTitle();
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
    public int compareTo(final @NotNull Base o) {
        return 0;
    }
}
