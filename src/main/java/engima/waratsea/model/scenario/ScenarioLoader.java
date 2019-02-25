package engima.waratsea.model.scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.AppProps;


import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class loads the scenario data from the scenario json files.
 *
 * Each scenario consists of several json files.
 * The summary.json file contains a brief description of the scenario.
 * The alliesTaskForces.json file contains the allied forces available for this scenario.
 * The axisTaskForces.json file contains the axis forces available for this scenario.
 */
@Slf4j
@Singleton
public class ScenarioLoader {
    private static final String SUMMARY_FILE_NAME = "summary.json";

    private GameTitle gameTitle;
    private AppProps props;

    /**
     * The constructor. Called by guice.
     * @param gameTitle The game title.
     * @param props Application properties.
     */
    @Inject
    public ScenarioLoader(final GameTitle gameTitle,
                          final AppProps props) {
        this.gameTitle = gameTitle;
        this.props = props;
    }

    /**
     * Load the game scenario summaries.
     * @return A list of scenarios.
     * @throws ScenarioException if the scenario summaries cannot be loaded.
     */
    public List<Scenario> load() throws ScenarioException {

        File[] directories = getScenarioDirs();                                                                         //Get the sub-directories directly under the scenario directory.
                                                                                                                        //Each scenario's data is stored in its own sub-directory.
        return  Arrays.stream(directories)
                .filter(this::isReadable)                                                                               //If the directory is not readable exclude it.
                .map(this::readScenarioSummary)
                .filter(Objects::nonNull)                                                                               //Filter any null scenarios, these occur when the json file fails to parse.
                .sorted()                                                                                               //Sort the scenarios.
                .collect(Collectors.toList());
    }

    /**
     * Get all the scenario directories.
     * @return An array of directory files.
     * @throws ScenarioException Thrown if unable to read the scenario files.
     */
    private File[] getScenarioDirs() throws ScenarioException {

        URL url = getClass().getClassLoader().getResource(gameTitle.getValue() + Config.SCENARIO_DIRECTORY_NAME);

        if (url == null) {
           throw new ScenarioException("Unable to find the scenario main directory");
        }

        File[] directories = new File(url.getPath()).listFiles(File::isDirectory);

        if (directories == null) {
           throw new ScenarioException("Unable to find the scenario sub directories");
        }

        return directories;
    }

    /**
     * Verify that the directory is readable. If the directory is not readable then we will exclude it from
     * the list of scenarios.
     * @param directory The scenario directory to test for readability.
     * @return True if the scenario directory can be read. False otherwise.
     */
    private boolean isReadable(final File directory) {
        boolean isFileReadable = directory.canRead();
        log.debug("file: {} is readable is {}", directory, isFileReadable);
        return isFileReadable;
    }

    /**
     * Read the scenario summary json file.
     * @param directory The directory that contains the scenario summary json file.
     * @return A Scenario object. null is returned if the json file fails to parse.
     */
    private Scenario readScenarioSummary(final File directory)  {
        Path path = Paths.get(directory.getPath(), SUMMARY_FILE_NAME);

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setDateFormat(props.getString("scenario.date.format")).create();
            Scenario scenario = gson.fromJson(br, Scenario.class);

            log.info("load scenario: {}", scenario.getTitle());

            return scenario;
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load scenario: {}", directory.getName(), ex);
            return null;                                                                                                // Null's should be removed from the scenario list.
        }
    }
}
