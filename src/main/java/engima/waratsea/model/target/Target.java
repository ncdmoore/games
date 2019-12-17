package engima.waratsea.model.target;


import engima.waratsea.model.PersistentData;
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
}
