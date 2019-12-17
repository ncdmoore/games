package engima.waratsea.model.target;

import com.google.inject.name.Named;
import engima.waratsea.model.target.data.TargetData;

/**
 * Factory used by guice to create task forces.
 */
public interface TargetFactory {
    /**
     * Creates an enemy airfield target.
     *
     * @param data Target data read from a JSON file.
     * @return A Target initialized with the data from the JSON file.
     */
    @Named("enemyAirfield")
    Target createEnemyAirfieldTarget(TargetData data);

    /**
     * Creates an friendly airfield target.
     *
     * @param data Target data read from a JSON file.
     * @return A Target initialized with the data from the JSON file.
     */
    @Named("friendlyAirfield")
    Target createFriendlyAirfieldTarget(TargetData data);

    /**
     * Creates an enemy port target.
     *
     * @param data Target data read from a JSON file.
     * @return A Target initialized with the data from the JSON file.
     */
    @Named("enemyPort")
    Target createEnemyPortTarget(TargetData data);

    /**
     * Creates a friendly port target.
     *
     * @param data Target data read from a JSON file.
     * @return A Target initialized with the data from the JSON file.
     */
    @Named("friendlyPort")
    Target createFriendlyPortTarget(TargetData data);

    /**
     * Creates a sea grid target. This is used in mine laying or clearing.
     *
     * @param data Target data read from a JSON file.
     * @return A target initialized with the deata from the JSON file.
     */
    @Named("seaGrid")
    Target createSeaGrid(TargetData data);

}
