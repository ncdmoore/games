package engima.waratsea.model.taskForce;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class loads and saves task force data.
 */
@Singleton
@Slf4j
public class TaskForceDAO {

    private Resource config;
    private TaskForceFactory taskForceFactory;

    /**
     * The constructor. Called by guice.
     * @param config The game's config.
     * @param taskForceFactory Factory for creating task force objects.
     */
    @Inject
    public TaskForceDAO(final Resource config,
                        final TaskForceFactory taskForceFactory) {
        this.config = config;
        this.taskForceFactory = taskForceFactory;
    }

    /**
     * Load the task force for the given scenario and side.
     *
     * @param scenario The selected scenario.
     * @param side The side: ALLIES or AXIS.
     * @return A list of task forces.
     * @throws ScenarioException if the scenario task force cannot be loaded.
     */
    public List<TaskForce> load(final Scenario scenario, final Side side) throws ScenarioException {
        log.info("Load task forces, scenario: '{}', side: '{}'", scenario.getTitle(), side);
        return getURL(side)
                .map(u -> readTaskForce(u, side))
                .orElseThrow(() -> new ScenarioException("Unable to load task force for " + scenario.getTitle() + " for " + side));
    }

    /**
     * Get the task force URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The task force URL.
     */
    private Optional<URL> getURL(final Side side) {
        return config.isNew() ? config.getScenarioURL(side, TaskForce.class) // Get the new game task forces.
                : config.getSavedURL(side, TaskForce.class);                 // Get a saved game task forces.
    }

    /**
     * Save the task force. The allies and axis task force data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side AlLIES of AXIS.
     * @param taskForces The task force data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<TaskForce> taskForces) {
        log.info("Saving task forces, scenario: '{}',side {}", scenario.getTitle(), side);
        String fileName = config.getSavedFileName(side, TaskForce.class);
        PersistentUtility.save(fileName, taskForces);
    }

    /**
     * Read the task force data from scenario task force json files for the given side.
     *
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

            log.debug("load task forces for side: {}, number of task forces: {}", side, taskForces.size());

            return buildTaskForce(side, taskForces);
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load task forces: {}", url.getPath(), ex);
            return null;
        }
    }

    /**
     * Seed the task forces with the data from the JSON file.
     *
     * @param side The side of the task force. ALLIES or AXIS.
     * @param data Task force data from the JSON file.
     * @return An initialized or seeded Task Force.
     */
    private List<TaskForce> buildTaskForce(final Side side, final List<TaskForceData> data) {
        return data.stream()
                .map(taskForceData -> taskForceFactory.create(side, taskForceData))
                .peek(this::initTaskForce)
                .collect(Collectors.toList());
    }

    /**
     * Initialize the task force.
     *
     * @param taskForce The task force that is initialized.
     */
    private void initTaskForce(final TaskForce taskForce) {
        if (config.isNew()) {
            taskForce.init();
        }
    }

}
