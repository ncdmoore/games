package engima.waratsea.model.flotilla;

import engima.waratsea.model.PersistentData;
import engima.waratsea.model.asset.Asset;
import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.vessel.Vessel;

import java.util.List;

/**
 * Represents a flotilla.
 */
public interface Flotilla extends Asset, PersistentData<FlotillaData> {

    /**
     * Determine if the flotilla is at a friendly port.
     *
     * @return True if the flotilla is currently located at a friendly port. False otherwise.
     */
    boolean atFriendlyBase();

    /**
     * Get the flotilla's reference. Return a port if the flotilla is in a port.
     *
     * @return The flotilla's reference. Mapped to a port name if the flotilla is in a port.
     */
    String getMappedLocation();

    /**
     * Set the flotilla's reference.
     *
     * @param location The flotilla's new reference.
     */
    void setReference(String location);

    /**
     * Flotilla's are always active.
     *
     * @return True.
     */
    boolean isActive();

    /**
     * Get the flotilla's state.
     *
     * @return Active. Flotilla's are always active.
     */
    TaskForceState getState();

    /**
     * Get the list of vessels that make up this flotilla.
     *
     * @return The vessels that make up the flotilla.
     */
    List<Vessel> getVessels();
}
