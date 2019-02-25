package engima.waratsea.model.victory;


import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class loads the victory data from the victory json files.
 * It loads the default victory data and it optionally loads scenario victory data.
 * Not every scenario has specific victory conditions, thus there may not be any
 * scenario victory data to load.
 *
 * Each victory consists
 */
@Slf4j
@Singleton
public class VictoryLoader {
    private Config config;
    private VictoryConditionsFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param config The game config
     * @param factory The victory conditions factory.
     */
    @Inject
    public VictoryLoader(final Config config,
                         final VictoryConditionsFactory factory) {
        this.config = config;
        this.factory = factory;
    }

    /**
     * Load the victory conditions. Load the default victory condition data and any scenario victory
     * condition data, if it exists. A scenario might not have any victory conditions.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The Victory
     * @throws VictoryException An error occurred while attempting to read the default victory data.
     */
    public VictoryConditions load(final Scenario scenario, final Side side) throws VictoryException {
        return (config.getType() == GameType.NEW) ? readNew(scenario, side) : readExisting(scenario, side);
    }

    /**
     * Load the victory conditions for a new game. Load the default victory condition data and any scenario victory
     * condition data, if it exists. A scenario might not have any victory conditions.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The Victory
     * @throws VictoryException An error occurred while attempting to read the default victory data.
     */
    private VictoryConditions readNew(final Scenario scenario, final Side side) throws VictoryException {
        VictoryConditionsData victoryConditionsData = loadDefaultVictoryData();

        try {
            // Create the specific scenario's victory conditions.
            VictoryConditionsData scenarioVictoryData = loadScenarioVictoryData(scenario, side);

            //Add the specific fields to the victory data.
            victoryConditionsData.setObjectives(scenarioVictoryData.getObjectives());
            victoryConditionsData.setScenarioShip(scenarioVictoryData.getScenarioShip());
            victoryConditionsData.setRequiredShip(scenarioVictoryData.getRequiredShip());

        } catch (VictoryException ex) {
            //Unable to load the scenario victory conditions. If a scenario does not contain specific
            //victory conditions then this is normal. Thus, all this code can do is warn that no
            //specific scenario victory data exists for the particular side.
            log.warn("No scencario victory found for scenario '{}' for side {}", scenario.getTitle(), side);
        }

        // Create the side's victory conditions.
        return factory.create(victoryConditionsData, side);
    }

    /**
     * Load the victory conditions for an existing(saved) game. All of the victory conditions (both the default and
     * the scenario specific victory conditions are in the same file.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The victory.
     * @throws VictoryException An error occurred while attempting to read the saved game victory data.
     */
    private VictoryConditions readExisting(final Scenario scenario, final Side side) throws VictoryException {
        VictoryConditionsData savedVictoryData = loadSavedGameVictoryData(scenario, side);
        return factory.create(savedVictoryData, side);
    }

    /**
     * Save the victory conditions. The allies and axis victory data is saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side AlLIES of AXIS.
     * @param data The victory conditions data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final VictoryConditionsData data) {
        log.info("Saving victory, scenario: {}, side: {}", scenario.getTitle(), side);
        String fileName = config.getSavedFileName(side, Victory.class);
        PersistentUtility.save(fileName, data);
    }

    /**
     * Read the default victory data from the JSON file.
     *
     * @return The default victory conditions for the game.
     *
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    private VictoryConditionsData loadDefaultVictoryData() throws VictoryException {
        log.info("Load default victory");
        return config
                .getDefaultURL(Victory.class)
                .map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load default victory"));
    }

    /**
     * Read the scenario specific data from the JSON file.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The scenario specific victory data.
     * @throws VictoryException Indicates that an error occurred while attempting to read the scenario victory data.
     */
    private VictoryConditionsData loadScenarioVictoryData(final Scenario scenario, final Side side) throws VictoryException {
        log.info("Load scenario victory, scenario: '{}', side: '{}'", scenario.getTitle(), side);
        return config
                .getScenarioURL(side, Victory.class)
                .map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load victory for side " + side, "warn"));
    }

    /**
     * Read an existing/saved game data from the JSON file.
     *
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The all the victory conditions for the given side.
     * @throws VictoryException Indicates that an error occured while attempting to read the saved game victory data.
     */
    private VictoryConditionsData loadSavedGameVictoryData(final Scenario scenario, final Side side) throws VictoryException {
        log.info("Load victory, scenario: '{}', side: '{}'", scenario.getTitle(), side);
        return config
                .getSavedURL(side, Victory.class)
                .map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load saved game victory for side " + side));
    }

    /**
     * Read the victory data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @return The data read from the JSON file.
     */
    private VictoryConditionsData readVictory(final URL url) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                VictoryConditionsData data = gson.fromJson(br, VictoryConditionsData.class);

                log.info("load victory '{}'", url.getPath());

                return data;
            } catch (Exception ex) {                                                                                        // Catch any Gson errors.
                log.error("Unable to load victory '{}'. {}", new Object[]{url.getPath(), ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load victory '{}'. {}", new Object[]{url.getPath(), ex});
            return null;
        }
    }
}
