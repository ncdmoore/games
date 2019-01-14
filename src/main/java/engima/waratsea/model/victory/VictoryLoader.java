package engima.waratsea.model.victory;


import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Side;
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

    private VictoryData victoryData;

    /**
     * Constructor called by guice.
     */
    @Inject
    public VictoryLoader() {

    }

    /**
     * Load the victory conditions.
     * @param side The side ALLIES or AXIS.
     * @return The Victory
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    public Victory build(final Side side) throws VictoryException {

        if (victoryData == null) {
            loadVictoryData();
        }

        return new Victory(victoryData, side);
    }

    /**
     * Read the default victory data from the JSON file.
     * @throws VictoryException An error occurred while attempting to read the victory data.
     */
    private void loadVictoryData() throws VictoryException {
        String path = VICTORY_DIRECTORY_NAME + "/default.json";
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        victoryData = url.map(this::readVictory)
                .orElseThrow(() -> new VictoryException("Unable to load default victory"));
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
                log.error("Unable to load default victory '{}'. {}", new Object[]{url.getPath(), ex});
                return null;
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Unable to load default victory '{}'. {}", new Object[]{url.getPath(), ex});
            return null;
        }
    }
}
