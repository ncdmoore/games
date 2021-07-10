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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents an aircraft carrier.
 *
 * This class can be viewed as a ship or an airbase. It implements both interfaces.
 */
public class AircraftCarrier implements Ship, Airbase {
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

    @Getter private final AirbaseType airbaseType = AirbaseType.CARRIER;
    @Getter private final FlightDeck flightDeck;
    private final AircraftCapacity aircraftCapacity;
    @Getter private final List<LandingType> landingType;
    @Getter private final boolean real = true;

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
     * @param region The aircraft carrier's region.
     */
    @Inject
    public AircraftCarrier(@Assisted final ShipData data,
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

        data.setLandingType(landingType);

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
        return shipId.getName().replaceAll("-\\d*$", "");
    }


    /**
     * Get the air base's nations.
     *
     * @return The air base's nations.
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
        return country == nation;
    }

    /**
     * Get the given nations region.
     *
     * @param squadronNation The nation.
     * @return The region that corresponds to the given nation.
     */
    @Override
    public Region getRegion(final Nation squadronNation) {
        return region;
    }

    /**
     * Get the region's title. Aircraft carriers do not have regions.
     *
     * @return The region's title.
     */
    @Override
    public String getRegionTitle() {
        return region.getTitle();
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
     * Get the base's game grid.
     *
     * @return The base's game grid.
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
        return squadrons.getCurrentSteps();
    }

    /**
     * Indicates if this airbase has any squadrons.
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
     * Get the squadron given its name.
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
     * Get the list of squadrons for the given nation.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc.
     * @return The squadron list for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation squadronNation) {
        return squadrons.getSquadrons(squadronNation);
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
        return squadrons.getSquadrons(squadronNation, state);
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
                .collect(Collectors.toMap(country -> country, this::getSquadrons));
    }

    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param country The nation: BRITISH, ITALIAN, etc.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation country) {
        return squadrons.getSquadronMap(country);
    }
    /**
     * Get the squadron map for the given nation and given squadron state.
     *
     * @param country The nation: BRITISH, ITALIAN, etc.
     * @param state The squadron state.
     * @return The squadron map keyed by aircraft type for the given nation and given squadron state.
     */
    @Override
    public Map<AircraftType, List<Squadron>> getSquadronMap(final Nation country, final SquadronState state) {
        return squadrons.getSquadronMap(country, state);
    }

    /**
     * Get a list of the aircraft models present at this airbase.
     *
     * Build a map of models to list of aircraft for that model. Then return the first element of each list.
     * This gives us a unique list of aircraft per model. Each model of aircraft appears in the list once.
     * Note, each sublist is guaranteed to contain at least one element.
     *
     * @param desiredNation The nation.
     * @return A unique list of aircraft that represent the aircraft models present at this airbase.
     */
    @Override
    public List<Aircraft> getAircraftModelsPresent(final Nation desiredNation) {
        return squadrons.getAircraftModelsPresent(desiredNation);
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
            squadrons.add(squadron);
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
    }

    /**
     * Get all of the aircraft carrier's air missions.
     *
     * @return A list of the air missions.
     */
    @Override
    public List<AirMission> getMissions() {
        return missions.getMissions();
    }

    /**
     * Get the current air missions of this aircraft carrier.
     *
     * @param squadronNation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the current air missions.
     */
    @Override
    public List<AirMission> getMissions(final Nation squadronNation) {
        return missions.getMissions(squadronNation);
    }

    /**
     * Add an air mission to this aircraft carrier.
     *
     * @param mission The air mission that is added to this aircraft carrier.
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
     * Get the Patrol specified by the given patrol type.
     *
     * @param patrolType The type of patrol.
     * @return The Patrol that corresponds to the given patrol type.
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
     * Upddate the patrol.
     *
     * @param patrolType The type of patrol.
     * @param patrolSquadrons  The squadrons on patrol.
     */
    @Override
    public void updatePatrol(final PatrolType patrolType, final List<Squadron> patrolSquadrons) {
        patrols.update(patrolType, patrolSquadrons);
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
     * Get the air base's anti aircraft rating.
     *
     * @return The air base's anti aircraft rating.
     */
    @Override
    public int getAntiAirRating() {
        return antiAir.getHealth();
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
     * Set the ships ammunition type. Aircraft carrier's don't have ammunition types.
     *
     * @param ammunitionType The ship's ammunition type.
     */
    @Override
    public void setAmmunitionType(final AmmunitionType ammunitionType) {
    }

    /**
     * Get the ship's ammunition type.
     *
     * @return The ship's ammunition type.
     */
    @Override
    public AmmunitionType getAmmunitionType() {
        return AmmunitionType.NORMAL;
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
    public int compareTo(@NotNull final Base o) {
        return getTitle().compareTo(o.getTitle());
    }
}
