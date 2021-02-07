package engima.waratsea.model.enemy.views.taskForce;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.BaseId;
import engima.waratsea.model.enemy.views.taskForce.data.TaskForceViewData;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.PersistentUtility;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
@Slf4j
public class TaskForceViewDAO {

    private final Resource resource;
    private final TaskForceViewFactory factory;

    /**
     * Constructor called by guice.
     *
     * @param resource The game resource configuration.
     * @param factory The task force view factory.
     */
    @Inject
    public TaskForceViewDAO(final Resource resource,
                            final TaskForceViewFactory factory) {
        this.resource = resource;
        this.factory = factory;
    }

    /**
     * Load the task force view.
     *
     * @param enemyTaskForces A list of task forces.
     * @return A list of enemy task force views.
     */
    public List<TaskForceView> load(final List<TaskForce> enemyTaskForces) {
        return  resource.isNew() ? getNew(enemyTaskForces) : getExisting(enemyTaskForces);
    }

    /**
     * Save the task force views. The allies and axis task force view data are saved in separate files.
     *
     * @param scenario The selected scenario.
     * @param side  The side ALLIES or AXIS.
     * @param taskForceViews The task force view data that is saved.
     */
    public void save(final Scenario scenario, final Side side, final List<TaskForceView> taskForceViews) {
        log.debug("Saving task force views, scenario: '{}',side {}", scenario.getTitle(), side);
        taskForceViews.forEach(taskForceView -> {
            String fileName = resource.getSavedFileName(side, TaskForceView.class, taskForceView.getName() + ".json");
            PersistentUtility.save(fileName, taskForceView);
            log.debug("Saving task force view: '{}'", taskForceView.getName());
        });
    }

    /**
     * Get the enemy task force views for a new game.
     *
     * @param enemyTaskForces A list of enemy task forces.
     * @return A list of enemy task force views.
     */
    private List<TaskForceView> getNew(final List<TaskForce> enemyTaskForces) {
        return enemyTaskForces
                .stream()
                .map(this::createTaskForceData)
                .map(this::createTaskForceView)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy task force views for an existing game.
     *
     * @param enemyTaskForces A list of enemy task forces.
     * @return A list of enemy task force views.
     */
    private List<TaskForceView> getExisting(final List<TaskForce> enemyTaskForces) {
        return enemyTaskForces
                .stream()
                .map(this::readTaskForceView)
                .filter(Objects::nonNull)
                .map(this::createTaskForceView)
                .collect(Collectors.toList());
    }

    /**
     * Create the task force view data. This is only valid for new games where little is known about the
     * enemy task force.
     *
     * @param taskForce The enemy task force.
     * @return The enemy task force view data.
     */
    private TaskForceViewData createTaskForceData(final TaskForce taskForce) {
        TaskForceViewData data = new TaskForceViewData();
        data.setName(taskForce.getName());
        data.setTaskForce(taskForce);
        return data;
    }

    /**
     * Create the task force view from the task force view data.
     *
     * @param data The task force view data.
     * @return The task force view of an enemy task force.
     */
    private TaskForceView createTaskForceView(final TaskForceViewData data) {
        return factory.create(data);
    }

    /**
     * Read the task force view data.
     *
     * @param taskForce The enemy task force.
     * @return The task force view data read in from a JSON file.
     */
    private TaskForceViewData readTaskForceView(final TaskForce taskForce) {

        BaseId taskForceId = new BaseId(taskForce.getName(), taskForce.getSide());

        return getURL(taskForceId)
                .map(url -> readTaskForce(url, taskForceId))
                .map(data -> addTaskForce(data, taskForce))
                .orElseGet(() -> logError(taskForceId));
    }

    /**
     * Get the URL of the enemy task force view.
     *
     * @param taskForceId The task force Id.
     * @return An optional URL corresponding to the specified enemy task force Id.
     */
    private Optional<URL> getURL(final BaseId taskForceId) {
        Side side = taskForceId.getSide();
        String portName = taskForceId.getName();
        return resource.getSavedURL(side.opposite(), TaskForceView.class, portName + ".json");
    }

    /**
     * Read the task force view data from the JSON file.
     *
     * @param url The url of the JSON file.
     * @param taskForceId Uniquely identifies a task force.
     * @return The data read from the JSON file.
     */
    private TaskForceViewData readTaskForce(final URL url, final BaseId taskForceId) {
        String taskForceIdName = taskForceId.getName();
        Side side = taskForceId.getSide();

        Path path = Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            TaskForceViewData taskForceViewData = gson.fromJson(br, TaskForceViewData.class);

            log.debug("load enemy task force view {} for side {}", taskForceIdName, side);

            return taskForceViewData;
        } catch (Exception ex) {                                                                                        // Catch any Gson errors.
            log.error("Unable to load enemy task force view {} for side: {}. {}", new Object[]{url.getPath(), side, ex});
            return null;
        }
    }

    /**
     * Add the task force to the tasl fprce data read in form the JSON file.
     *
     * @param data The task force view data read in from the JSON file.
     * @param taskForce The task force that is added.
     * @return The modified task force view data.
     */
    private TaskForceViewData addTaskForce(final TaskForceViewData data, final TaskForce taskForce) {
        data.setTaskForce(taskForce);
        return data;
    }

    /**
     * Log an error for task forces that cannot be loaded.
     *
     * @param taskForceId The task force Id that cannot be loaded.
     * @return null. The calling routine will filter this out.
     */
    private TaskForceViewData logError(final BaseId taskForceId) {
        log.error("Unable to load enemy task force view '{}'", taskForceId.getName());
        return null;
    }
}
