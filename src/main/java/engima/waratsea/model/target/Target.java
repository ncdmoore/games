package engima.waratsea.model.target;


import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;


/**
 * A task force or air strike targets.
 */
public interface Target extends PersistentData<TargetData> {
    /**
     * Get the name of the target. This will correspond to one of the opponent side's assets: task force, airfield or port.
     *
     * @return The target's name.
     */
    String getName();

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
    String getLocation();

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
     * Determine if the given squadron is in range of this target.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    boolean inRange(Squadron squadron);

    /**
     * Determine if the given squadron is allowed to attack this target.
     *
     * @param squadron The squadron that is checked if allowed to attack this target.
     * @return True if this target may attack this target. False otherwise.
     */
    boolean mayAttack(Squadron squadron);

    /**
     * Get the distance to this target from the given airbase.
     *
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    int getDistance(Airbase airbase);

    /**
     * Determine if the airbase that is the target has capacity to support additional squadron steps.
     *
     * @param excludedAirbase An airbase to exclude in determining the number of mission
     *                        steps currently assigned to this target's region.
     * @param currentAirbaseMissionSteps The current airbase mission steps. This is the
     *                                   airbase that is currently being edited in the GUI.
     * @return True if this target's airbase has capacity to accept more squadron steps. False otherwise.
     */
    boolean hasAirbaseCapacity(Airbase excludedAirbase, int currentAirbaseMissionSteps);

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
    boolean hasRegionCapacity(Nation nation, Airbase excludedAirbase, int currentAirbaseMissionSteps);

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
    int getCapacitySteps();

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    int getCurrentSteps();

    /**
     * Get the maximum number of squadron steps of this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The maximum number of squadron steps of this target's region.
     */
    int getRegionMaxSteps(Nation nation);

    /**
     * Get the current number of squadron steps of this target's region.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The current number of squadron steps of this target's region.
     */
    int getRegionCurrentSteps(Nation nation);

    /**
     * Get the current number of squadron steps on missions that originate
     * outside of this target's region that are assigned targets in the
     * same region as this target.
     *
     * @param missionType The type of mission.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param airbase The airbase that contains the mission that has this target as a target.
     * @return The total number of squadron steps with the given mission type
     * that originate outside of the region of this target, but have a
     * target in the same region as this target.
     */
    int getMissionStepsEnteringRegion(AirMissionType missionType, Nation nation, Airbase airbase);

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
    int getMissionStepsLeavingRegion(AirMissionType missionType, Nation nation, Airbase airbase);
}
