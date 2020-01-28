package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TargetFriendlyTaskForce implements Target {

    private final Game game;
    private final GameMap gameMap;

    @Getter
    private final String name;

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
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getLocation() {
        return Optional
                .ofNullable(taskForce)
                .orElseGet(this::getTaskForce)
                .getReference();
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
        return getTaskForce();
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
        return name;
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
     * Get the distance to this target from the given airbase.
     *
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    @Override
    public int getDistance(final Airbase airbase) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = airbase.getReference();

        return gameMap.determineDistance(targetReference, airbaseReference);
    }

    /**
     * Determine if the given squadron is in range of this target.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    @Override
    public boolean inRange(final Squadron squadron) {
        return  getTaskForce()
                .getLandingType()
                .contains(squadron.getLandingType())
                && isInRange(squadron);

    }

    /**
     * Get the total number of squadron steps that assigned this target.
     *
     * @return The total number of squadron steps that are assigned this target.
     */
    @Override
    public int getTotalSteps(final Airbase airbase) {
        return game
                .getPlayer(side)
                .getAirfields()
                .stream()
                .filter(base -> base != airbase)
                .map(base -> base.getTotalSteps(this))
                .reduce(0, Integer::sum);
    }

    /**
     * Get the total number of squadron steps that may be assigned to this target.
     *
     * @return The total number of squadron steps that may be assigned to this target.
     */
    @Override
    public int getTotalCapacitySteps() {
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
     * Determine if the given squadron is in range of this target.
     *
     * @param squadron The squadron that may or may not be in range of this target.
     * @return True if the given squadron is in range. False otherwise.
     */
    private boolean isInRange(final Squadron squadron) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = squadron.getAirfield().getReference();

        return gameMap.inRange(airbaseReference, targetReference, squadron.getFerryDistance());
    }

    /**
     * Get the port view for this target.
     *
     * @return This target's port view.
     */
    private TaskForce getTaskForce() {
        taskForce = game.getPlayer(side)
                .getTaskForceMap()
                .get(name);

        if (taskForce == null) {
            log.error("Cannot find task force: '{}' for side: '{}'", name, side);
        }

        return taskForce;
    }
}
