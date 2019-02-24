package engima.waratsea.model.victory;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.GameType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private static final String ALLIED_VICTORY_FILE_NAME = "/alliesVictory.json";
    private static final String AXIS_VICTORY_FILE_NAME = "/axisVictory.json";

    private static final Map<Side, String> FILE_NAME_MAP = new HashMap<>();
    static {
        FILE_NAME_MAP.put(Side.ALLIES, ALLIED_VICTORY_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS, AXIS_VICTORY_FILE_NAME);
    }

    private GameTitle gameTitle;
    private VictoryConditionsFactory factory;
    private VictoryConditionsData victoryConditionsData;

    /**
     * Constructor called by guice.
     *
     * @param gameTitle The game title.
     * @param factory The victory conditions factory.
     */
    @Inject
    public VictoryLoader(final GameTitle gameTitle, final VictoryConditionsFactory factory) {
        this.gameTitle = gameTitle;
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
    public VictoryConditions read(final Scenario scenario, final Side side) throws VictoryException {
        if (gameTitle.getType() == GameType.NEW) {
            return readNew(scenario, side);
        } else {
            return readExisting(scenario, side);
        }
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
        victoryConditionsData = loadDefaultVictoryData();

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

        String fileName = Config.SAVED_GAME_DIRECTORY + scenario.getName() + gameTitle.getSavedGameName() + "/" + Config.VICTORY_DIRECTORY_NAME + "/" + side.toString() + "Victory.json";
        log.info("Saving victory for side {} path: '{}'", side, fileName);

        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());

            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(data);

            FileWriter writer = new FileWriter(fileName);
            writer.write(json);
            writer.close();

        } catch (IOException ex) {
            log.error("Unable to save victory '{}'", fileName, ex);
        }

    }

    /**
     * Read the default victory data from the JSON file.
     *
     * @return The default victory conditions for the game.
     *
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    private VictoryConditionsData loadDefaultVictoryData() throws VictoryException {
        String path = Config.VICTORY_DIRECTORY_NAME + "/default.json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(this::readVictory)
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
        String path = gameTitle.getValue() + "/scenarios/" + scenario.getName() + FILE_NAME_MAP.get(side);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(this::readVictory)
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
        String fileName = Config.SAVED_GAME_DIRECTORY + scenario.getName()  + gameTitle.getSavedGameName() + "/" + Config.VICTORY_DIRECTORY_NAME + "/" + side.toString() + "Victory.json";
        Path path = Paths.get(fileName);
        try {
            URL url = path.toUri().toURL();
            return readVictory(url);
        } catch (MalformedURLException ex) {
            throw new VictoryException("Unable to load saved game victory for side " + side);
        }
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
