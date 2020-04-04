package engima.waratsea.model.scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.AppProps;
import engima.waratsea.model.game.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
public class ScenarioDAO {
    private Resource config;
    private AppProps props;

    /**
     * The constructor. Called by guice.
     *
     * @param config The game's config.
     * @param props Application properties.
     */
    @Inject
    public ScenarioDAO(final Resource config,
                       final AppProps props) {
        this.config = config;
        this.props = props;
    }

    /**
     * Load the game scenario summaries.
     *
     * @return A list of scenarios.
     * @throws ScenarioException if the scenario summaries cannot be loaded.
     */
    public List<Scenario> load() throws ScenarioException {

        File[] directories = getScenarioDirs();                                                                         //Get the sub-directories directly under the scenario directory.
                                                                                                                        //Each scenario's data is stored in its own sub-directory.
        return  Arrays.stream(directories)
                .filter(this::isReadable)                                                                               //If the directory is not readable exclude it.
                .map(this::readScenarioSummary)
                .filter(Optional::isPresent)                                                                            //Filter any null scenarios, these occur when the json file fails to parse.
                .sorted()                                                                                               //Sort the scenarios.
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Get all the scenario directories.
     *
     * @return An array of directory files.
     * @throws ScenarioException Thrown if unable to read the scenario files.
     */
    private File[] getScenarioDirs() throws ScenarioException {
        return config.getScenarioDirectory()
                .map(url -> new File(url.getPath()).listFiles(File::isDirectory))
                .orElseThrow(() -> new ScenarioException("Unable to find the scenario directories"));
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
     * Read the scenario summary json file.
     *
     * @param directory The directory that contains the scenario summary json file.
     * @return A Scenario object. null is returned if the json file fails to parse.
     */
    private Optional<Scenario> readScenarioSummary(final File directory)  {
        Path path = config.getScenarioSummary(directory.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setDateFormat(props.getString("scenario.date.format")).create();
            Scenario scenario = gson.fromJson(br, Scenario.class);

            log.debug("load scenario: {}", scenario.getTitle());

            return Optional.of(scenario);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load scenario: {}", directory.getName(), ex);
            return Optional.empty();                                                                                                // Null's should be removed from the scenario list.
        }
    }
}
