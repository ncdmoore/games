package engima.waratsea.model.victory;


import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.victory.data.VictoryData;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
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
 *
 * Each victory consists
 */
@Slf4j
@Singleton
public class VictoryLoader {

    private static final String VICTORY_DIRECTORY_NAME = "victory";
    private static final String ALLIED_VICTORY_FILE_NAME = "/alliesVictory.json";
    private static final String AXIS_VICTORY_FILE_NAME = "/axisVictory.json";

    private static final Map<Side, String> FILE_NAME_MAP = new HashMap<>();
    static {
        FILE_NAME_MAP.put(Side.ALLIES, ALLIED_VICTORY_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS, AXIS_VICTORY_FILE_NAME);
    }

    private GameTitle gameTitle;
    private VictoryConditionsFactory factory;
    private VictoryData defaultVictoryData;
    private VictoryData scenarioVictoryData;

    /**
     * Constructor called by guice.
     * @param gameTitle The game title.
     * @param factory The victory conditions factory.
     */
    @Inject
    public VictoryLoader(final GameTitle gameTitle, final VictoryConditionsFactory factory) {
        this.gameTitle = gameTitle;
        this.factory = factory;
    }

    /**
     * Load the victory conditions.
     * @param scenario The selected scenario.
     * @param side The side ALLIES or AXIS.
     * @return The Victory
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    public VictoryConditions build(final Scenario scenario, final Side side) throws VictoryException {

        log.info("here");

        if (defaultVictoryData == null) {
            loadDefaultVictoryData();
        }

        // Create the side's victory conditions.
        VictoryConditions conditions = factory.create(defaultVictoryData, side);

        try {
            // Create the specific scenario's victory conditions.
            loadScenarioVictoryData(scenario, side);
            conditions.addScenarioConditions(scenarioVictoryData);
        } catch (VictoryException ex) {
            log.warn("No scencario victory found for scenario '{}' for side {}", scenario.getTitle(), side);
        }

        return conditions;
    }

    /**
     * Read the default victory data from the JSON file.
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    private void loadDefaultVictoryData() throws VictoryException {
        String path = VICTORY_DIRECTORY_NAME + "/default.json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        defaultVictoryData = url.map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load default victory"));
    }

    private void loadScenarioVictoryData(final Scenario scenario, final Side side) throws VictoryException {
        String path = gameTitle.getValue() + "/scenarios/" + scenario.getName() + FILE_NAME_MAP.get(side);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        scenarioVictoryData = url.map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load scenario victory for side " + side, "warn"));
    }

    /**
     * Read the victory data from the JSON file.
     * @param url The url of the JSON file.
     * @return The data read from the JSON file.
     */
    private VictoryData readVictory(final URL url) {
        try {
            Path path = Paths.get(URLDecoder.decode(url.getPath(), "UTF-8"));
            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                VictoryData data = gson.fromJson(br, VictoryData.class);

                log.info("load default victory '{}'", url.getPath());

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
