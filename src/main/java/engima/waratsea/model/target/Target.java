package engima.waratsea.model.target;


import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;

import java.util.List;
import java.util.Optional;


/**
 * A task force or air strike targets.
 */
public interface Target extends Comparable<Target>, PersistentData<TargetData> {
    /**
     * Get the name of the target. This will correspond to one of the opponent side's assets: task force, airfield or port.
     *
     * @return The target's name.
     */
    String getName();

    /**
     * Get the side of the target.
     *
     * @return The target's side.
     */
    Side getSide();

    /**
     * Get the title of the target. This is the title of the opponent's asset.
     *
     * @return The target's title.
     */
    String getTitle();

    /**
     * Get the target's map region.
     *
     * @return The target's map region.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    Region getRegion(Nation nation);

    /**
     * Get the title of the region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The target's region title.
     */
    String getRegionTitle(Nation nation);

    /**
     * Get the location of the target. This is the current location of the target. It's map reference.
     *
     * @return The target's location.
     */
    String getReference();

    /**
     * Get the target's game grid.
     *
     * @return The target's game grid.
     */
    Optional<GameGrid> getGrid();

    /**
     * Get the target persistent data.
     *
     * @return The target's persistent data.
     */
    TargetData getData();

    /**
     * Get the underlying object of the target. This is the opponent's asset.
     *
     * @return The underlying object of the target.
     */
    Object getView();

    /**
     * The squadrons land at this target. This is only used by friendly airbases.
     *
     * @param squadrons The squadrons that land at this target.
     */
    default void land(MissionSquadrons squadrons) {
    }

    /**
     * Augment or create a patrol of the given type over this target.
     *
     * @param patrolType The type of patrol.
     * @param squadrons The squadrons that are added to the given patrol over the target.
     */
    default void augmentPatrol(PatrolType patrolType, List<Squadron> squadrons) {
    }

    /**
     * Reduce or delete a patrol of the given type over this target.
     *
     * @param patrolType The type of patrol.
     * @param squadrons The squadrons that are removed from the given patrol over the target.
     */
    default void reducePatrol(PatrolType patrolType, List<Squadron> squadrons) {
    }

    /**
     * The squadrons attack this target.
     *
     * @param squadrons The squadrons that attack this target.
     */
    void resolveAttack(MissionSquadrons squadrons);

    /**
     * The squadrons sweep this target.
     *
     * @param squadrons The squadrons that sweep this target.
     */
    void resolveSweep(MissionSquadrons squadrons);

    /**
     * Get the String representation of the target.
     *
     * @return The String representation of this target.
     */
    String toString();

    /**
     * Determine if this target is equal to the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    boolean isEqual(Target target);

    /**
     * Determine if a squadron journeying to this target must make a round trip.
     *
     * @return True if the squadron must make a round trip to reach this target. The squadron must fly to and then
     * return to its original base. False otherwise.
     */
    boolean requiresRoundTrip();

    /**
     * Determine if the given squadron is allowed to perform the given mission against this target.
     *
     * @param type The air mission type.
     * @param squadron The squadron that is checked to determine is allowed to perform the given mission against this target.
     * @return True if given squadron may perform the given mission against this target. False otherwise.
     */
    default boolean isMissionAllowed(AirMissionType type, Squadron squadron) {
        return true;
    }

    /**
     * Get the distance to this target from the given airbase.
     *
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    int getDistance(Airbase airbase);

    /**
     * Get the distance to this target from the given game grid.
     *
     * @param grid The game grid whose distance to target is returned.
     * @return The distance to this target from the given game grid.
     */
    int getDistance(GameGrid grid);

    /**
     * Determine if the airbase that is the target has capacity to support additional squadron steps.
     *
     * @param excludedAirbase An airbase to exclude in determining the number of mission
     *                        steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's airbase has capacity to accept more squadron steps. False otherwise.
     */
    default boolean hasAirbaseCapacity(Airbase excludedAirbase, int currentAirbaseMissionSteps) {
        return true;
    }

    /**
     * Determine if the region that contains this target has capacity to support additional
     * squadron steps.
     *
     * @param nation The region's nation.
     * @param excludedAirbase An airbase to exclude in determining the number of mission
     *                        steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's region has capacity to accept more squadron steps. False otherwise.
     */
    default boolean hasRegionCapacity(Nation nation, Airbase excludedAirbase, int currentAirbaseMissionSteps) {
        return true;
    }

    /**
     * Get the total number of squadron steps that are on missions with this as a target.
     *
     * @param airbase The airbase that contains the mission that has this target as a target.
     * @return The total number of squadron steps that are assigned this target.
     */
    int getMissionSteps(Airbase airbase);

    /**
     * Get the total number of squadron steps that may be assigned to this target.
     *
     * @return The total number of squadron steps that may be assigned to this target.
     */
    default int getCapacitySteps() {
        return 0;
    }

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    default int getCurrentSteps() {
        return 0;
    }

    /**
     * Get the maximum number of squadron steps of this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The maximum number of squadron steps of this target's region.
     */
    default int getRegionMaxSteps(Nation nation) {
        return 0;
    }

    /**
     * Get the current number of squadron steps of this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current number of squadron steps of this target's region.
     */
    default int getRegionCurrentSteps(Nation nation) {
        return 0;
    }

    /**
     * Get the current number of squadron steps on missions that originate
     * outside this target's region that are assigned targets in the
     * same region as this target.
     *
     * @param missionType The type of mission.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airbase The airbase that contains the mission that has this target as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate outside the region of this target, but have a
     * target in the same region as this target.
     */
    default int getMissionStepsEnteringRegion(AirMissionType missionType, Nation nation, Airbase airbase) {
        return 0;
    }

    /**
     * Get the current number of squadron steps on missions of the given type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     *
     * @param missionType The type of mission.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airbase The airbase that contains the mission that has this as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate in the same region as the given airbase and that have targets
     * in different regions than the airbase region.
     */
    default int getMissionStepsLeavingRegion(AirMissionType missionType, Nation nation, Airbase airbase) {
        return 0;
    }
}
