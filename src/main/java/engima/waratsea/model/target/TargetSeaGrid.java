package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a sea grid target. This is used by mine laying and mine clearing missions.
 */
@Slf4j
public class TargetSeaGrid implements Target {
    private final String reference;
    private final GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param gameMap The game map.
     */
    @Inject
    public TargetSeaGrid(@Assisted final TargetData data,
                                   final GameMap gameMap) {
        this.gameMap = gameMap;
        reference = data.getName();
    }

    /**
     * Get the name of the target.
     *
     * @return The target's name.
     */
    @Override
    public String getName() {
        return reference;
    }

    /**
     * Land grid's have no side.
     *
     * @return The neutral side.
     */
    @Override
    public Side getSide() {
        return Side.NEUTRAL;
    }

    /**
     * Get the title of the target.
     *
     * @return The target's title.
     */
    @Override
    public String getTitle() {
        return reference;
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
        return reference;
    }

    /**
     * Get the target's game grid.
     *
     * @return The target's game grid.
     */
    @Override
    public Optional<GameGrid> getGrid() {
        return gameMap.getGrid(reference);
    }

    /**
     * Get the target persistent data.
     *
     * @return The target's persistent data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setType(TargetType.SEA_GRID);
        data.setName(reference);
        return data;
    }

    /**
     * Get the underlying object of the target.
     *
     * @return The underlying object of the target.
     */
    @Override
    public Object getView() {
        return reference;
    }

    /**
     * Perform a patrol of the given type over this target.
     *
     * @param patrolType The type of patrol.
     * @param squadrons  The squadron that perform this patrol over the target.
     */
    @Override
    public void augmentPatrol(final PatrolType patrolType, final List<Squadron> squadrons) {

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
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return reference;
    }

    /**
     * Determine if this target is equal or is the same as the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    @Override
    public boolean isEqual(final Target target) {
        return reference == target.getView();
    }

    /**
     * Determine if a squadron journeying to this target must make a round trip.
     *
     * @return True if the squadron must make a round trip to reach this target. The squadron must fly to and then
     * return to it's original base. False otherwise.
     */
    @Override
    public boolean requiresRoundTrip() {
        return true;
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
     * Get the total number of squadron steps that assigned this target.
     *
     * @return The total number of squadron steps that are assigned this target.
     */
    @Override
    public int getMissionSteps(final Airbase airbase) {
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

        return reference.equals(otherTarget.getReference());
    }

    /**
     * The hash code for this object.
     *
     * @return hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(reference);
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
