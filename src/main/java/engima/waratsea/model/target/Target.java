package engima.waratsea.model.target;


import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;


/**
 * A task force or air strike targets.
 */
public interface Target extends PersistentData<TargetData> {
    /**
     * Get the name of the target.
     *
     * @return The target's name.
     */
    String getName();

    /**
     * Get the location of the target.
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
     * Get the underlying object of the target.
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
     * Determine if this target is equal or is the same as the given target.
     *
     * @param target the target that this target is tested for equality.
     * @return True if this target is equal to the given target. False, otherwise.
     */
    boolean isEqual(Target target);

    /**
     * Determine if this squadron is in range of the given squadron.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    boolean inRange(Squadron squadron);

    /**
     * Get the distance to this target from the given airbase.
     *
     * @param airbase The airbase whose distance to target is returned.
     * @return The distance to this target from the given airbase.
     */
    int getDistance(Airbase airbase);

    /**
     * Get the total number of squadron steps that assigned this target.
     *
     * @param airbase The airbase that contains the mission that has this target  as a target.
     * @return The total number of squadron steps that are assigned this target.
     */
    int getTotalSteps(Airbase airbase);

    /**
     * Get the total number of squadron steps that may be assigned to this target.
     *
     * @return The total number of squadron steps that may be assigned to this target.
     */
    int getTotalCapacitySteps();

    /**
     * Get the number of squadron steps that are currently assigned to this target.
     *
     * @return The number of squadron steps that are currently assigned to this target.
     */
    int getCurrentSteps();
}
