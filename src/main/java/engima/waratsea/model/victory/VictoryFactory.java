package engima.waratsea.model.victory;

import engima.waratsea.model.victory.data.VictoryData;

/**
 * Factory used by guice to create task forces.
 */
public interface VictoryFactory {

    /**
     * Create's the victory.
     * @param data Victory data from a JSON file.
     * @return The Victory object.
     */
    Victory create(VictoryData data);
}
