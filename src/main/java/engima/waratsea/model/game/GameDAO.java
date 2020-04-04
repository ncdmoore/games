package engima.waratsea.model.game;

import com.google.gson.Gson;
import com.google.inject.Inject;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class loads and saves game data.
 */
@Slf4j
public class GameDAO {
    private Resource config;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     */
    @Inject
    public GameDAO(final Resource config) {
        this.config = config;
    }

    /**
     * Load the game for the given scenario and side.
     *
     * @return The game data.
     * @throws ScenarioException if the game cannot be loaded.
     */
    public List<GameData> load() throws ScenarioException {
        log.info("Load games");

        File[] directories = getScenarioDirs();                                                                         //Get the sub-directories directly under the scenario directory.

        return Arrays.stream(directories)
                .filter(this::isReadable)
                .flatMap(this::getSavedGameDirs)
                .map(this::getGameDataFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::readGame)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Save the game data.
     *
     * @param game The game that is saved.
     */
    public void save(final Game game) {
        log.info("Saving game");
        String fileName = config.getSavedFileName(Game.class);
        PersistentUtility.save(fileName, game);
    }

    /**
     * Get all the scenario directories.
     *
     * @return An array of directory files.
     * @throws ScenarioException Thrown if unable to read the scenario files.
     */
    private File[] getScenarioDirs() throws ScenarioException {
        return config.getSavedDirectory()
                .map(url -> new File(url.getPath()).listFiles(File::isDirectory))
                .orElseThrow(() -> new ScenarioException("Unable to find the scenario directories"));
    }

    /**
     * Get the saved game directories of the given scenario.
     *
     * @param scenarioDirectory A given scenario directory in the saved games directory.
     * @return A stream of saved game directories.
     */
    private Stream<File> getSavedGameDirs(final File scenarioDirectory)  {
        File[] savedGameDirs = scenarioDirectory.listFiles(File::isDirectory);
        Optional<File[]> dirs = Optional.ofNullable(savedGameDirs);
        return Arrays.stream(dirs.orElse(new File[0]));
    }

    /**
     * Get the URL of the saved game "game.json" file.
     *
     * @param file The saved game directory.
     * @return The URL of the saved game.json file.
     */
    private Optional<URL> getGameDataFile(final File file) {
        String fileName = file.getPath() + "/game.json";

        Path path = Paths.get(fileName);
        try {
            return Optional.of(path.toUri().toURL());
        } catch (MalformedURLException ex) {
            log.error("Bad url '{}'", path);
            return Optional.empty();
        }
    }

    /**
     * Verify that the directory is readable. If the directory is not readable then we will exclude it from
     * the list of scenarios.
     *
     * @param directory The scenario directory to test for readability.
     * @return True if the scenario directory can be read. False otherwise.
     */
    private boolean isReadable(final File directory) {
        boolean isFileReadable = directory.canRead();
        log.debug("file: {} is readable is {}", directory, isFileReadable);
        return isFileReadable;
    }

    /**
     * Read the task force data from scenario task force json files for the given side.
     *
     * @param url specifies the task force json file.
     * @return returns a list of task force objects.
     */
    private Optional<GameData> readGame(final URL url) {
        try {
            Path path = Paths.get(url.toURI().getPath());

            log.info("load game data with path '{}'", path);

            try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

                Gson gson = new Gson();
                return Optional.of(gson.fromJson(br, GameData.class));
            } catch (Exception ex) {                                                                                    // Catch any Gson errors.
                log.error("Unable to read game data for URL: {}", url.getPath(), ex);
                return Optional.empty();
            }
        } catch (URISyntaxException ex) {
            log.error("Unable to get URI for URL: {}", url.getPath());
            return Optional.empty();
        }
    }
}
