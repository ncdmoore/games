package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a friendly airbase target. This is used in squadron ferry missions.
 * The destination airbase is a friendly airbase target. This allows ferry missions
 * to work like all other missions. A friendly airbase may be an airfield or a ship
 * that implements the airbase interface such as an aircraft carrier or capital ship.
 */
@Slf4j
public class TargetFriendlyAirbase implements Target {
    private final Game game;
    private final GameMap gameMap;

    @Getter
    private final String name;

    @Getter
    private final Side side;

    //private int priority;

    private Airbase airbase;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public TargetFriendlyAirbase(@Assisted final TargetData data,
                                 final Game game,
                                 final GameMap gameMap) {
        this.game = game;
        this.gameMap = gameMap;

        name = data.getName();
        side = data.getSide();
    }

    /**
     * Get the title of the target.
     *
     * @return The target's title.
     */
    @Override
    public String getTitle() {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getTitle();
    }

    /**
     * Get the target's map region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The target's map region.
     */
    @Override
    public Region getRegion(final Nation nation) {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getRegion(nation);
    }


    /**
     * Get the title of the region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The target's region title.
     */
    @Override
    public String getRegionTitle(final Nation nation) {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getRegion(nation).getName();
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getReference() {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getReference();
    }

    /**
     * Get the target's game grid.
     *
     * @return The target's game grid.
     */
    @Override
    public Optional<GameGrid> getGrid() {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getGrid();
    }

    /**
     * Get the target data that is persisted.
     *
     * @return The persistent target data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setType(TargetType.FRIENDLY_AIRBASE);
        data.setName(name);
        return data;
    }

    /**
     * Get the underlying object of the target.
     *
     * @return The underlying object of the target.
     */
    @Override
    public Object getView() {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase);
    }

    /**
     * The squadrons land at this target. This is only used by friendly airbases.
     *
     * @param squadrons The squadrons that land at this target.
     */
    @Override
    public void land(final MissionSquadrons squadrons) {
        squadrons
                .getAll()
                .forEach(squadron -> airbase.addSquadron(squadron));

        squadrons.land();
    }

    /**
     * Perform a patrol of the given type over this target.
     *
     * @param patrolType The type of patrol.
     * @param squadrons  The squadron that perform this patrol over the target.
     */
    @Override
    public void patrol(final PatrolType patrolType, final List<Squadron> squadrons) {

    }

    /**
     * The squadrons attack this target.
     *
     * @param squadrons The squadrons that attack this target.
     */
    @Override
    public void resolveAttack(final MissionSquadrons squadrons) {
        land(squadrons);
    }

    /**
     * The squadrons sweep this target.
     *
     * @param squadrons The squadrons that sweep this target.
     */
    @Override
    public void resolveSweep(final MissionSquadrons squadrons) {
        land(squadrons);
    }

    /**
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return Optional
                .ofNullable(airbase)
                .orElseGet(this::getAirbase)
                .getTitle();
    }

    /**
     * Determine if this target is equal or is the same as the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    @Override
    public boolean isEqual(final Target target) {
        return getAirbase() == target.getView();
    }

    /**
     * Determine if a squadron journeying to this target must make a round trip.
     *
     * @return True if the squadron must make a round trip to reach this target. The squadron must fly to and then
     * return to it's original base. False otherwise.
     */
    @Override
    public boolean requiresRoundTrip() {
        return false;
    }

    /**
     * Get the distance to this target from the given airbase.
     *
     * @param originAirbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    @Override
    public int getDistance(final Airbase originAirbase) {
        String targetReference = gameMap.convertNameToReference(getReference());
        String airbaseReference = originAirbase.getReference();

        return gameMap.determineDistance(targetReference, airbaseReference);
    }

    /**
     * Get the distance to this target from the given game grid.
     *
     * @param grid The game grid whose distance to target is returned.
     * @return The distance to this target from the given game grid.
     */
    @Override
    public int getDistance(final GameGrid grid) {
        String targetReference = gameMap.convertNameToReference(getReference());
        return gameMap.determineDistance(targetReference, grid.getMapReference());
    }

    /**
     * Determine if the given squadron is allowed to perform the given mission against this target.
     *
     * @param type The air mission type.
     * @param squadron The squadron that is checked to determine is allowed to perform the given mission against this target.
     * @return True if given squadron may perform the given mission against this target. False otherwise.
     */
    @Override
    public boolean isMissionAllowed(final AirMissionType type, final Squadron squadron) {
        return getAirbase()
                .getLandingType()
                .contains(squadron.getLandingType());
    }

    /**
     * Get the total number of squadron steps that are assigned this target excluding
     * missions from the given airbase.
     *
     * The excluded airbase is typically the airbase were missions are being
     * created. This airbase's missions are kept in the view and thus need
     * to be excluded from the model here.
     *
     * @param excludedAirbase The airbase from which the mission is launched with this as
     *                        a target. This airbase is excluded in the total
     *                        steps returned.
     * @return The total number of squadron steps that are assigned this target.
     */
    @Override
    public int getMissionSteps(final Airbase excludedAirbase) {
        return game
                .getPlayer(side)
                .getAirfields()
                .stream()
                .filter(base -> base != excludedAirbase)
                .map(base -> base.getTotalMissionSteps(this))
                .reduce(0, Integer::sum);
    }

    /**
     * Get the total number of squadron steps that may be assigned to this target.
     *
     * @return The total number of squadron steps that may be assigned to this target.
     */
    @Override
    public int getCapacitySteps() {
        return getAirbase().getCapacity();
    }

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    @Override
    public int getCurrentSteps() {
        return getAirbase().getCurrentSteps().intValue();
    }

    /**
     * Get the maximum number of squadron steps of the target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The maximum number of squadron steps of the target's region.
     */
    @Override
    public int getRegionMaxSteps(final Nation nation) {
        return getAirbase()
                .getRegion(nation)
                .getMaxSteps();
    }

    /**
     * Get the current number of squadron steps of the this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current number of squadron steps of this target's region.
     */
    @Override
    public int getRegionCurrentSteps(final Nation nation) {
        return getAirbase()
                .getRegion(nation)
                .getCurrentSteps();
    }

    /**
     * Get the current number of squadron steps on missions that originate
     * outside of this target's region that are assigned targets in the
     * same region as this target.
     *
     * @param missionType The type of mission.
     * @param nation      The nation: BRITISH, ITALIAN, etc...
     * @param originAirbase     The airbase that contains the mission that has this target as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate outside of the region of this target, but have a
     * target in the same region as this target.
     */
    @Override
    public int getMissionStepsEnteringRegion(final AirMissionType missionType, final Nation nation, final Airbase originAirbase) {
        return game
                .getPlayer(side)
                .getAirfields()
                .stream()
                .filter(a -> a != originAirbase)
                .filter(a -> a.getRegion(nation) != getRegion(nation))
                .flatMap(a -> a
                        .getMissions(nation)
                        .stream()
                        .filter(mission -> mission.getTarget().getRegion(nation) == getRegion(nation)))
                .filter(mission -> mission.getType() == AirMissionType.FERRY)
                .map(mission -> mission.getSquadrons().getSteps())
                .reduce(0, Integer::sum);
    }

    /**
     * Get the current number of squadron steps on missions of the given type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     *
     * @param missionType The type of mission.
     * @param nation      The nation: BRITISH, ITALIAN, etc...
     * @param originAirbase     The airbase that contains the mission that has this as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     */
    @Override
    public int getMissionStepsLeavingRegion(final AirMissionType missionType, final Nation nation, final Airbase originAirbase) {
        return game
                .getPlayer(side)
                .getAirfields()
                .stream()
                .filter(a -> a != originAirbase)
                .filter(a -> a.getRegion(nation) == originAirbase.getRegion(nation))
                .flatMap(a -> a
                        .getMissions(nation)
                        .stream()
                        .filter(mission -> mission.getTarget().getRegion(nation) != a.getRegion(nation)))
                .filter(mission -> mission.getType() == AirMissionType.FERRY)
                .map(mission -> mission.getSquadrons().getSteps())
                .reduce(0, Integer::sum);
    }

    /**
     * Determine if the airbase that is the target has capacity to support additional squadron steps.
     *
     * @param excludedAirbase An airbase to exclude in determining the number of mission
     *                        steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's airbase has capacity to accept more squadron steps. False otherwise.
     */
    @Override
    public boolean hasAirbaseCapacity(final Airbase excludedAirbase, final int currentAirbaseMissionSteps) {
        int airbaseMaxSteps = getAirbase().getCapacity();

        int airbaseCurrentSteps = getAirbase().getCurrentSteps().intValue();
        int airbaseMissionSteps = getMissionSteps(excludedAirbase);    // excludes current airbase.

        return airbaseMaxSteps >= airbaseCurrentSteps + airbaseMissionSteps + currentAirbaseMissionSteps;
    }

    /**
     * Determine if the region that contains this target can has capacity to support additional
     * squadron steps.
     *
     * @param nation The region's nation.
     * @param excludedAirbase An airbase to exclude in determining the number of mission
     *                        steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's region has capacity to accept more squadron steps. False otherwise.
     */
    @Override
    public boolean hasRegionCapacity(final Nation nation, final Airbase excludedAirbase, final int currentAirbaseMissionSteps) {
        Region region = getAirbase().getRegion(nation);  // The region of the target.

        // If the target is in the same region as the excluded air base, then the region must have capacity.
        // This is the case where the airbase that originates the mission and the target are both in the same region.
        // Since the squadrons are not changing regions, the region must have capacity.
        if (region == excludedAirbase.getRegion(nation)) {
            return true;
        }

        int regionCurrentSteps = region.getCurrentSteps();
        int regionMaxSteps = region.getMaxSteps();

        // This is the total squadron steps from all the player's missions of type ferry that
        // originate in regions other than this target's region.
        int regionMissionSteps = getMissionStepsEnteringRegion(AirMissionType.FERRY, nation, excludedAirbase);

        // If the region's maximum allowed steps is zero, this indicates the region does not have a maximum.
        // Thus, a region with a maximum allowed steps of zero always has capacity.
        return regionMaxSteps == 0 || regionMaxSteps >= regionCurrentSteps + regionMissionSteps + currentAirbaseMissionSteps;
    }

    /**
     * Determine if this Target is equal to a given target.
     *
     * @param o The other target.
     * @return True if this target is equal to the other target.
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Target)) {
            return false;
        }

        if (o == this) {
            return true;
        }

        Target otherTarget = (Target) o;

        return side == otherTarget.getSide() && name.equals(otherTarget.getName()) && getReference().equals(otherTarget.getReference());
    }

    /**
     * The hash code for this object.
     *
     * @return hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, side);
    }


    /**
     * Get the airbase view for this target.
     *
     * @return This target's airbase view.
     */
    private Airbase getAirbase() {
        airbase = game
                .getPlayer(side)
                .getAirbase(name);

        if (airbase == null) {
            log.error("Cannot find airbase view: '{}'", name);
        }

        return airbase;
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
    public int compareTo(@NotNull final Target o) {
        return getTitle().compareTo(o.getTitle());
    }
}
