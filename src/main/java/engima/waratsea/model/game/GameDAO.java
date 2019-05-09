package engima.waratsea.model.game;

import com.google.gson.Gson;
import com.google.inject.Inject;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class loads and saves game data.
 */
@Slf4j
public class GameDAO {


    private Config config;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     */
    @Inject
    public GameDAO(final Config config) {
        this.config = config;
    }

    /**
     * Load the task force for the given scenario and side.
     *
     * @return The game data.
     * @throws GameException if the game cannot be loaded.
     */
    public GameData load() throws GameException {
        log.info("Load game");
        return config
                .getSavedURL(Game.class)
                .map(this::readGame)
                .orElseThrow(() -> new GameException("Unable to game"));
    }

    /**
     * Save the game data.
     *
     * @param data The game data that is saved.
     */
    public void save(final GameData data) {
        log.info("Saving game");
        String fileName = config.getSavedFileName(Game.class);
        PersistentUtility.save(fileName, data);
    }

    /**
     * Read the task force data from scenario task force json files for the given side.
     *
     * @param url specifies the task force json file.
     * @return returns a list of task force objects.
     */
    private GameData readGame(final URL url) {
        Path path = Paths.get(url.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            log.info("load game data");
            Gson gson = new Gson();
            return gson.fromJson(br, GameData.class);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to game data", ex);
            return null;
        }
    }
}
