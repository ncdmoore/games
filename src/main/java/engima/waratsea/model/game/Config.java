package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.victory.Victory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Global game configuration parameters.
 */
@Slf4j
@Singleton
public final class Config {
    public static final String MAP_DIRECTORY_NAME = "/maps";
    public static final String SCENARIO_DIRECTORY_NAME = "/scenarios";
    public static final String AIRFIELD_DIRECTORY_NAME = "/airfields";
    public static final String PORT_DIRECTORY_NAME = "/ports";

    private static final String VICTORY_DIRECTORY_NAME = "victory";
    private static final String TASK_FORCE_DIRECTORY_NAME = "taskforce";

    private static final String SAVED_GAME_DIRECTORY = System.getProperty("user.home") + "/WW2atSea/SavedGames/";

    private static final String ALLIES_TASK_FORCE_FILE_NAME = TASK_FORCE_DIRECTORY_NAME + "/alliesTaskForces.json";
    private static final String AXIS_TASK_FORCE_FILE_NAME = TASK_FORCE_DIRECTORY_NAME + "/axisTaskForces.json";
    private static final String ALLIED_VICTORY_FILE_NAME = VICTORY_DIRECTORY_NAME + "/alliesVictory.json";
    private static final String AXIS_VICTORY_FILE_NAME = VICTORY_DIRECTORY_NAME + "/axisVictory.json";

    private static final MultiKeyMap<String, String> FILE_NAME_MAP = new MultiKeyMap<>();
    static {
        FILE_NAME_MAP.put(Side.ALLIES.toString(), TaskForce.class.getSimpleName(), ALLIES_TASK_FORCE_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS.toString(),   TaskForce.class.getSimpleName(), AXIS_TASK_FORCE_FILE_NAME);
        FILE_NAME_MAP.put(Side.ALLIES.toString(), Victory.class.getSimpleName(),   ALLIED_VICTORY_FILE_NAME);
        FILE_NAME_MAP.put(Side.AXIS.toString(),   Victory.class.getSimpleName(),   AXIS_VICTORY_FILE_NAME);
    }

    private static final Map<Class<?>, String> DEFAULT_FILE_NAME_MAP = new HashMap<>();
    static {
        DEFAULT_FILE_NAME_MAP.put(Victory.class, VICTORY_DIRECTORY_NAME);
    }

    private static final String DEFAULT_SAVED_GAME = "/defaultGame";

    private GameTitle gameTitle;


    @Getter
    @Setter
    private GameType type;

    @Setter
    private Scenario scenario;


    @Getter
    @Setter
    private String savedGameName;    //This is the name of the saved game.

    /**
     * Constructor called by guice.
     *
     * @param gameTitle The game title.
     */
    @Inject
    public Config(final GameTitle gameTitle) {
        this.gameTitle = gameTitle;
        this.type = GameType.NEW;
        savedGameName = DEFAULT_SAVED_GAME;
    }

    /**
     * Get the task force URL.
     *
     * @param side The side ALLIES or AXIS.
     * @return The task force URL.
     */
    public Optional<URL> getTaskForceURL(final Side side) {
        return (type == GameType.NEW) ? getScenarioURL(side, TaskForce.class) : getSavedURL(side, TaskForce.class);
    }

    /**
     * Get the scenario URL. This URL maps to a file in the scenario directory.

     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The task force URL.
     */
    public Optional<URL> getScenarioURL(final Side side, final Class<?> clazz) {
        String entityName = clazz.getSimpleName();
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario.getName() + "/" + FILE_NAME_MAP.get(side.toString(), entityName);
        log.info("'{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get the default URL. This URL maps to the default folder of the game for the given entity.
     *
     * @param clazz The entity class.
     * @return The default victory URL.
     */
    public Optional<URL> getDefaultURL(final Class<?> clazz) {
        String fileName = DEFAULT_FILE_NAME_MAP.get(clazz) + "/default.json";
        log.info("Default '{}' URL: '{}'", clazz.getSimpleName(), fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get the saved URL of the given entity. This URL maps to a file in the current saved game directory.
     *
     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The URL of the entity.
     */
    public Optional<URL> getSavedURL(final Side side, final Class<?> clazz) {
        String fileName = getSavedFileName(side, clazz);
        Path path = Paths.get(fileName);
        try {
            return Optional.of(path.toUri().toURL());
        } catch (MalformedURLException ex) {
            log.error("Bad url '{}'", path);
            return Optional.empty();
        }
    }

    /**
     * Get the saved file path of the given entity. This path maps to a file in the current saved game directory.
     *
     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The file path of the entity's saved game.
     */
    public String getSavedFileName(final Side side, final Class<?> clazz) {
        String entityName = clazz.getSimpleName();
        String fileName = SAVED_GAME_DIRECTORY + scenario.getName() + savedGameName + "/"  + FILE_NAME_MAP.get(side.toString(), entityName);
        log.info("'{}' URL: '{}'", entityName, fileName);
        return fileName;
    }

}
