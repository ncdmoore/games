package engima.waratsea.model.victory;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game victory conditions and status.
 * This class contains the victory conditions for both sides ALLIES and AXIS.
 */
@Slf4j
@Singleton
public class Victory {

    private Map<Side, VictoryConditions> conditions = new HashMap<>();
    private VictoryLoader loader;

    /**
     * Constructor called by guice.
     *
     * @param loader The victory conditions loader.
     */
    @Inject
    public Victory(final VictoryLoader loader) {
        this.loader = loader;
    }

    /**
     * Load the victory conditions for both sides.
     *
     * @param scenario The selected scenario.
     * @throws VictoryException Indicates that the victory conditions could not be loaded.
     */
    public void load(final Scenario scenario) throws VictoryException {
        conditions.put(Side.ALLIES, loader.load(scenario, Side.ALLIES));
        conditions.put(Side.AXIS, loader.load(scenario, Side.AXIS));
    }

    /**
     * Get the victory objectives for the given side.
     *
     * @param side The side ALLIES or AXIS.
     * @return The sides victory objectives.
     */
    public String getObjectives(final Side side) {
        return conditions.get(side).getObjectives();
    }

    /**
     *  Save the current victory data. The allied and axis data is saved separately.
     *
     * @param scenario The selected scenario.
     */
    public void save(final Scenario scenario) {
       loader.save(scenario, Side.ALLIES, conditions.get(Side.ALLIES));
       loader.save(scenario, Side.AXIS, conditions.get(Side.AXIS));
    }
}
