package engima.waratsea.model.target;

import engima.waratsea.model.target.data.TargetData;

/**
 * Factory used by guice to create task forces.
 */
public interface TargetFactory {
    /**
     * Creates a Target.
     *
     * @param data Target data read from a JSON file.
     * @return A Target initialized with the data from the JSON file.
     */
    Target create(TargetData data);
}
