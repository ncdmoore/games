package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Represents an enemy task force target.
 */
@Slf4j
public class TargetEnemyTaskForce implements Target {

    private Game game;
    private GameMap gameMap;

    @Getter
    private String name;

    private Side side;

    private TaskForceView taskForceView;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public TargetEnemyTaskForce(@Assisted final TargetData data,
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
        return getTaskForceViewView().getTitle();
    }

    /**
     * Get the target's map region.
     *
     * @return The target's map region.
     * @param nation The nation: BRITISH, ITALIAN, etc...
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
    public String getLocation() {
        return Optional
                .ofNullable(taskForceView)
                .orElseGet(this::getTaskForceViewView)
                .getLocation();
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
        data.setType(TargetType.ENEMY_TASK_FORCE);
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
        return getTaskForceViewView();
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
        return getTaskForceViewView() == target.getView();
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
     * Determine if this squadron is in range of the given squadron.
     *
     *
     * @param missionRole The squadron's mission role.
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    @Override
    public boolean inRange(final MissionRole missionRole, final Squadron squadron) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = squadron.getAirfield().getReference();

        // Drop tanks cannot be used on a strike mission if the fighter is performing the main strike role.
        int radius = missionRole == MissionRole.MAIN ? squadron.getMinRadius() : squadron.getMaxRadius();

        return gameMap.inRange(airbaseReference, targetReference, radius);
    }

    /**
     * Determine if the given squadron is in range of this target without needing external drop tanks.
     *
     * @param squadron The squadron that is determined to be in or out of range without external drop tanks.
     * @return True if this target is in range of the given squadron without using drop tanks. False otherwise.
     */
    @Override
    public boolean inRangeWithoutDropTanks(final Squadron squadron) {
        String targetReference = gameMap.convertNameToReference(getLocation());
        String airbaseReference = squadron.getAirfield().getReference();

        return gameMap.inRange(airbaseReference, targetReference, squadron.getMinRadius());
    }

    /**
     * Determine if the given squadron is allowed to attack this target.
     *
     * @param squadron The squadron that is checked if allowed to attack this target.
     * @return True if this target may attack this target. False otherwise.
     */
    @Override
    public boolean mayAttack(final Squadron squadron) {
        return true;
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
     * @param airbase The airbase that contains the mission that has this target as a target.
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
     * Get the port view for this target.
     *
     * @return This target's port view.
     */
    private TaskForceView getTaskForceViewView() {
        taskForceView = game.getPlayer(side)
                .getEnemyTaskForceMap()
                .get(name);

        if (taskForceView == null) {
            log.error("Cannot find task force view: '{}' for side: '{}'", name, side);
        }

        return taskForceView;
    }
}
