package engima.waratsea.model.game.nation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.utility.ProperyWrapper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents all the nation properties.
 */
@Singleton
public class NationProps {

    private final Config config;
    private final ProperyWrapper props;
    private static final String NATION_PROPERTIES = "/properties/nation.properties";

    /**
     * The constructor of the nation properties. First, the default nation properties are loaded and then the scenario
     * specific nation properties are loaded. This way a game specific property may overwrite a default application
     * property. Note, not all scenario's define nation properties.
     *
     * @param config The game config.
     * @param gameTitle The game's title/name.
     * @param props Property wrapper.
     */
    @Inject
    public NationProps(final Config config,
                       final GameTitle gameTitle,
                       final ProperyWrapper props) {
        this.config = config;
        props.init(gameTitle.getValue() + NATION_PROPERTIES);                                                     // Load game specific properties.
        this.props = props;
    }

    /**
     * Add scenario specific properties.
     *
     * @param scenario The selected scenario.
     */
    public void setScenario(final Scenario scenario) {
       props.init(config.getScenarioDirectoryNameName() + "/nation.properties");
    }

    /**
     * Get a string property.
     *
     * @param key The name of the property to get.
     * @return The property value.
     */
    public String getString(final String key) {
        return props.getString(key);
    }

    /**
     * Get the side property.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The side property value.
     */
    public List<Nation> getNations(final Scenario scenario, final Side side) {
        String nations = getString(side.toString() + "-" + scenario.getYear());
        return Arrays.stream(nations.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Nation::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Get an integer property.
     *
     * @param key The name of the property to get.
     * @return The property value.
     */
    public int getInt(final String key) {
        return props.getInt(key);
    }
}
