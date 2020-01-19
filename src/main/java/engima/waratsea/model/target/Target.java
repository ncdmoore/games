package engima.waratsea.model.target;


import engima.waratsea.model.PersistentData;
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
     * Get the String representation of the target.
     *
     * @return The String representation of this target.
     */
    String toString();

    /**
     * Determine if this squadron is in range of the given squadron.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    boolean inRange(Squadron squadron);
}
