package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.Patrols;
import engima.waratsea.model.base.airfield.squadron.Squadrons;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.SeaRegion;
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

/**
 * This class represents a virtual aircraft carrier. Every task force contains a virtual aircraft carrier.
 * It is used to hold land based patrols that patrol over the task force. This is just an adapter class
 * that allows the land based patrols to take advantage of all of the task force patrol code.
 *
 * When a land based CAP mission is launched against a friendly task force the squadrons on that mission
 * are added to this virtual aircraft carriers squadrons and to the is virtual aircraft carrier's patrol
 * squadrons.
 */
public class VirtualAircraftCarrier implements Ship, Airbase {
    private final Game game;

    @Getter private final ShipId shipId;                       // unique id.
    @Getter private final ShipType type;
    @Getter private final List<LandingType> landingType;
    @Getter private final AirbaseType airbaseType;
    @Getter private final int maxCapacity = 0;                // Virtual airbase has no capacity.
    @Getter private final int antiAirRating = 0;              // Virtual airbase has no AA.
    @Getter private final int capacity = 0;                   // Virtual airbase has no capacity.
    @Getter @Setter private TaskForce taskForce;

    private final Squadrons squadrons;
    private final Patrols patrols;
    private final Region region;

    @Inject
    public VirtualAircraftCarrier(@Assisted final ShipData data,
                                  final Squadrons squadrons,
                                  final Patrols patrols,
                                  final SeaRegion region,
                                  final Game game) {
        this.squadrons = squadrons;
        this.patrols = patrols;
        this.region = region;
        this.game = game;

        this.shipId = data.getShipId();
        type = data.getType();

        landingType = null;
        airbaseType = null;

        squadrons.build(this, data.getSquadronsData());
        patrols.build(this, data.getPatrolsData());
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
        data.setSquadronsData(squadrons.getData());
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
     * Get the ship's origin port. This is tracked at the ship level because a ship may be moved from one task
     * force to another. Thus, the ship must keep track of its origin port itself.
     *
     * @return The port the ship sailed from.
     */
    @Override
    public String getOriginPort() {
        return null;
    }

    /**
     * Get the ship's class. Not the java class, but the class of ship.
     *
     * @return The ship's class.
     */
    @Override
    public String getShipClass() {
        return null;
    }

    /**
     * Determines if this ship is an capable of air operations. It is either an aircraft carrier or a ship with
     * float planes.
     *
     * @return True if this ship is an aircraft carrier or float plane capable. False otherwise.
     */
    @Override
    public boolean isAirbase() {
        return false;
    }

    /**
     * Get the ship's nationality.
     *
     * @return The ship's nationality.
     */
    @Override
    public Nation getNation() {
        return null;
    }

    /**
     * Get the ship's victory points if sunk.
     *
     * @return The ship's victory points.
     */
    @Override
    public int getVictoryPoints() {
        return 0;
    }

    /**
     * Get the ship's primary gun.
     *
     * @return The ship's primary gun.
     */
    @Override
    public Gun getPrimary() {
        return null;
    }

    /**
     * Get the ship's secondary gun.
     *
     * @return The ship's secondary gun.
     */
    @Override
    public Gun getSecondary() {
        return null;
    }

    /**
     * Get the ship's tertiary gun.
     *
     * @return The ship's tertiary gun.
     */
    @Override
    public Gun getTertiary() {
        return null;
    }

    /**
     * Get the ship's anti-air gun.
     *
     * @return The ship's anti-air gun.
     */
    @Override
    public Gun getAntiAir() {
        return null;
    }

    /**
     * Get the ship's torpedo.
     *
     * @return The ship's torpedo.
     */
    @Override
    public Torpedo getTorpedo() {
        return null;
    }

    /**
     * Get the ship's ASW capability.
     *
     * @return The ship's ASW capability.
     */
    @Override
    public Asw getAsw() {
        return null;
    }

    /**
     * Get the ship's hull.
     *
     * @return The ship's hull.
     */
    @Override
    public Hull getHull() {
        return null;
    }

    /**
     * Get the ship's movement.
     *
     * @return The ship's movement.
     */
    @Override
    public Movement getMovement() {
        return null;
    }

    /**
     * Get the ship's cargo.
     *
     * @return The ship's cargo.
     */
    @Override
    public Cargo getCargo() {
        return null;
    }

    /**
     * Get the ship's fuel.
     *
     * @return The ship's fuel.
     */
    @Override
    public Fuel getFuel() {
        return null;
    }

    /**
     * Get the ship's ammunition type.
     *
     * @return The ship's ammunition type.
     */
    @Override
    public AmmunitionType getAmmunitionType() {
        return null;
    }

    /**
     * Get a list of all the ship components.
     *
     * @return A list of ship components.
     */
    @Override
    public List<Component> getComponents() {
        return null;
    }

    /**
     * Call this method to inform the ship that it is sailing from port.
     */
    @Override
    public void setSail() {

    }

    /**
     * Call this method to load a ship to its maximum cargoShips capacity.
     */
    @Override
    public void loadCargo() {

    }

    /**
     * Set the ships ammunition type.
     *
     * @param ammunitionType The ship's ammunition type.
     */
    @Override
    public void setAmmunitionType(final AmmunitionType ammunitionType) {

    }

    /**
     * Get the region of the squadron's home base.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The squadron's home base's region.
     */
    @Override
    public Region getRegion(final Nation nation) {
        return region;
    }

    /**
     * Get the region's title. The regions title should be independent of the nation. If nations share a region, the
     * region is represented by a separate java region java object for each nation. This is because each nation's region
     * has separate requirements. However, the actual map region is the same. Thus the title is the same.
     *
     * @return The region's title.
     */
    @Override
    public String getRegionTitle() {
        return region.getTitle();
    }

    /**
     * Get the map reference of the squadron's home base.
     *
     * @return The squadron's home base's map reference.
     */
    @Override
    public String getReference() {
        return taskForce.getReference();
    }

    /**
     * Get the airbase's game grid.
     *
     * @return The airbase's game grid.
     */
    @Override
    public Optional<GameGrid> getGrid() {
        return taskForce.getGrid();
    }

    /**
     * Get the airbase's nations. These are the nations that are permitted to station squadrons at this airbase.
     * All of a side's nations are allowed to station squadrons at a virtual airbase.
     *
     * @return The nations that are allowed to station squadrons at this airbase.
     */
    @Override
    public Set<Nation> getNations() {
        return game
                .getPlayer(shipId.getSide())
                .getNations();
    }

    /**
     * Determine if the given nation may use or station squadrons at this airbase.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return True if the given nation can use this airbase. False otherwise.
     */
    @Override
    public boolean canUse(final Nation nation) {
        return false;
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
        return AirfieldOperation.SUCCESS;
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
     * Squadron cannot be stationed at a virtual aircraft carrier.
     * However, they can be placed on patrol with a virtual aircraft
     * carrier. Thus, this method must return to true to allow any
     * squadron to be placed on patrol.
     *
     * @param squadron The squadron that is checked to determine if this airbase is its home.
     * @return True always.
     */
    @Override
    public boolean isStationed(final Squadron squadron) {
        return true;
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
        return AirfieldOperation.SUCCESS;
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
        return Collections.emptyList();
    }

    /**
     * Get the current missions of this airbase for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current squadron missions of this airbase for the given nation.
     */
    @Override
    public List<AirMission> getMissions(final Nation nation) {
        return Collections.emptyList();
    }

    /**
     * Add a mission to this airbase.
     *
     * @param mission The mission that is added to this airbase.
     */
    @Override
    public void addMission(final AirMission mission) {
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
        return 0;
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
        return null;
    }

    /**
     * Clear all missions, patrols and squadrons for this airbase.
     */
    @Override
    public void clear() {
        patrols.clear();
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

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }
}
