package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a friendly task force target. This is used by escort task force missions.
 */
@Slf4j
public class TargetFriendlyTaskForce implements Target {

    private final Game game;
    private final GameMap gameMap;

    @Getter
    private final String name;

    @Getter
    private final Side side;

    //private int priority;`

    private TaskForce taskForce;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public TargetFriendlyTaskForce(@Assisted final TargetData data,
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
                .ofNullable(taskForce)
                .orElseGet(this::getTaskForce)
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
        return null;
    }

    /**
     * Get the title of the region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The target's region title.
     */
    @Override
    public String getRegionTitle(final Nation nation) {
        return null;
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getReference() {
        return Optional
                .ofNullable(taskForce)
                .orElseGet(this::getTaskForce)
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
                .ofNullable(taskForce)
                .orElseGet(this::getTaskForce)
                .getGrid();
    }

    /**
     * Get the target persistent data.
     *
     * @return The target's persistent data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setName(name);
        data.setType(TargetType.FRIENDLY_TASK_FORCE);
        data.setSide(side);
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
                .ofNullable(taskForce)
                .orElseGet(this::getTaskForce);
    }

    /**
     * The squadrons land at this target. This is only used by friendly airbases.
     *
     * @param squadrons The squadrons that land at this target.
     */
    @Override
    public void land(final MissionSquadrons squadrons) {

    }

    /**
     * The squadrons attack this target.
     *
     * @param squadrons The squadrons that attack this target.
     */
    @Override
    public void resolveAttack(final MissionSquadrons squadrons) {

    }

    /**
     * The squadrons sweep this target.
     *
     * @param squadrons The squadrons that sweep this target.
     */
    @Override
    public void resolveSweep(final MissionSquadrons squadrons) {

    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {

    }

    /**
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Determine if this target is equal or is the same as the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    @Override
    public boolean isEqual(final Target target) {
        return getTaskForce() == target.getView();
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
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    @Override
    public int getDistance(final Airbase airbase) {
        String targetReference = gameMap.convertNameToReference(getReference());
        String airbaseReference = airbase.getReference();

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
     * Determine if the airbase that is the target has capacity to support additional squadron steps.
     *
     * @param excludedAirbase            An airbase to exclude in determining the number of mission
     *                                   steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's airbase has capacity to accept more squadron steps. False otherwise.
     */
    @Override
    public boolean hasAirbaseCapacity(final Airbase excludedAirbase, final int currentAirbaseMissionSteps) {
        return true;
    }

    /**
     * Determine if the region that contains this target can has capacity to support additional
     * squadron steps.
     *
     * @param nation                     The region's nation.
     * @param excludedAirbase            An airbase to exclude in determining the number of mission
     *                                   steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's region has capacity to accept more squadron steps. False otherwise.
     */
    @Override
    public boolean hasRegionCapacity(final Nation nation, final Airbase excludedAirbase, final int currentAirbaseMissionSteps) {
        return true;
    }

    /**
     * Determine if the given squadron is allowed to attack this target.
     *
     * @param squadron The squadron that is checked if allowed to attack this target.
     * @return True if this target may attack this target. False otherwise.
     */
    @Override
    public boolean mayAttack(final Squadron squadron) {
        return  getTaskForce()
                .getLandingType()
                .contains(squadron.getLandingType());
    }

    /**
     * Get the total number of squadron steps that assigned this target.
     *
     * @return The total number of squadron steps that are assigned this target.
     */
    @Override
    public int getMissionSteps(final Airbase airbase) {
        return game
                .getPlayer(side)
                .getAirfields()
                .stream()
                .filter(base -> base != airbase)
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
        return 0;
    }

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    @Override
    public int getCurrentSteps() {
        return 0;
    }

    /**
     * Get the maximum number of squadron steps of the target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The maximum number of squadron steps of the target's region.
     */
    @Override
    public int getRegionMaxSteps(final Nation nation) {
        return 0;
    }

    /**
     * Get the current number of squadron steps of the this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current number of squadron steps of this target's region.
     */
    @Override
    public int getRegionCurrentSteps(final Nation nation) {
        return 0;
    }

    /**
     * Get the current number of squadron steps on missions that originate
     * outside of this target's region that are assigned targets in the
     * same region as this target.
     *
     * @param missionType The type of mission.
     * @param nation      The nation: BRITISH, ITALIAN, etc...
     * @param airbase     The airbase that contains the mission that has this target as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate outside of the region of this target, but have a
     * target in the same region as this target.
     */
    @Override
    public int getMissionStepsEnteringRegion(final AirMissionType missionType, final Nation nation, final Airbase airbase) {
        return 0;
    }

    /**
     * Get the current number of squadron steps on missions of the given type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     *
     * @param missionType The type of mission.
     * @param nation      The nation: BRITISH, ITALIAN, etc...
     * @param airbase     The airbase that contains the mission that has this as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     */
    @Override
    public int getMissionStepsLeavingRegion(final AirMissionType missionType, final Nation nation, final Airbase airbase) {
        return 0;
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
     * Get the port view for this target.
     *
     * @return This target's port view.
     */
    private TaskForce getTaskForce() {
        taskForce = game
                .getPlayer(side)
                .getTaskForce(name);

        if (taskForce == null) {
            log.error("Cannot find task force: '{}' for side: '{}'", name, side);
        }

        return taskForce;
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
