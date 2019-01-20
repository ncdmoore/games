package engima.waratsea.model.scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.model.taskForce.TaskForceFactory;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.AppProps;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.taskForce.TaskForce;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class ScenarioLoader {

    private static final String SCENARIO_DIRECTORY_NAME = "/scenarios";
    private static final String SUMMARY_FILE_NAME = "summary.json";

    private static final String ALLIES_TASK_FORCE_FILE_NAME = "/alliesTaskForces.json";
    private static final String AXIS_TASK_FORCE_FILE_NAME = "/axisTaskForces.json";

    private static final Map<Side, String> FILE_NAME_MAP = new HashMap<>();
    static {
        FILE_NAME_MAP.put(Side.ALLIES, ALLIES_TASK_FORCE_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS, AXIS_TASK_FORCE_FILE_NAME);
    }

    private GameTitle gameTitle;
    private AppProps props;
    private TaskForceFactory taskForceFactory;

    /**
     * The constructor. Called by guice.
     * @param gameTitle The game title.
     * @param props Application properties.
     * @param taskForceFactory Factory for creating task force objects.
     */
    @Inject
    public ScenarioLoader(final GameTitle gameTitle,
                          final AppProps props,
                          final TaskForceFactory taskForceFactory) {
        this.gameTitle = gameTitle;
        this.props = props;
        this.taskForceFactory = taskForceFactory;
    }

    /**
     * Load the game scenario summaries.
     * @return A list of scenarios.
     * @throws ScenarioException if the scenario summaries cannot be loaded.
     */
    public List<Scenario> loadSummaries() throws ScenarioException {

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
     * Load the task force for the given scenario and side.
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of task forces.
     * @throws ScenarioException if the scenario task force cannot be loaded.
     */
    public List<TaskForce> loadTaskForce(final Scenario scenario, final Side side) throws ScenarioException {
        String scenarioName = scenario.getName();
        String path = gameTitle.getValue() + "/" + SCENARIO_DIRECTORY_NAME + "/" + scenarioName + FILE_NAME_MAP.get(side);
        Optional<URL> url = Optional.ofNullable(getClass().getClassLoader().getResource(path));
        return url.map(u -> readTaskForce(u, side))
                .orElseThrow(() -> new ScenarioException("Unable to load task force for " + scenarioName + " for " + side));
    }

    /**
     * Get all the scenario directories.
     * @return An array of directory files.
     * @throws ScenarioException Thrown if unable to read the scenario files.
     */
    private File[] getScenarioDirs() throws ScenarioException {

        URL url = getClass().getClassLoader().getResource(gameTitle.getValue() + SCENARIO_DIRECTORY_NAME);

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

    /**
     * Read the task force data from scenario task force json files for the given side.
     * @param url specifies the task force json file.
     * @param side specifies the side: ALLIES or AXIS.
     * @return returns a list of task force objects.
     */
    private List<TaskForce> readTaskForce(final URL url, final Side side) {
        Path path = Paths.get(url.getPath());

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Type collectionType = new TypeToken<List<TaskForceData>>() { }.getType();

            Gson gson = new Gson();
            List<TaskForceData> taskForces = gson.fromJson(br, collectionType);

            log.info("load task forces for side: {}, number of task forces: {}", side, taskForces.size());

            return seedTaskForces(side, taskForces);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load task forces: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Seed the task forces with the data from the JSON file.
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Task force data from the JSON file.
     * @return An initialized or seeded Task Force.
     */
    private List<TaskForce> seedTaskForces(final Side side, final List<TaskForceData> data) {
        return data.stream()
                .map(taskForceData -> taskForceFactory.create(side, taskForceData))
                .collect(Collectors.toList());
    }

}
