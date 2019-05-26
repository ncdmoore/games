package engima.waratsea.model.squadron;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.data.SquadronData;

/**
 * Factory used by guice to create squadrons.
 */
public interface SquadronFactory {
    /**
     * Creates a Squadron.
     *
     * @param side The side of the squadron. ALLIES or AXIS.
     * @param nation The nation.
     * @param data Squadron data read from a JSON file.
     * @return A Squadron initialized with the data from the JSON file.
     */
    Squadron create(Side side, Nation nation, SquadronData data);
}
