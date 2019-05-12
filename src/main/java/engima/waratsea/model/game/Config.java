package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.deployment.Deployment;
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
    private static final String SUMMARY_FILE_NAME = "summary.json";
    private static final String SCENARIO_DIRECTORY_NAME = "/scenarios";
    private static final String SAVED_GAME_DIRECTORY = System.getProperty("user.home") + "/WW2atSea/SavedGames/";

    private static final MultiKeyMap<String, String> SIDE_FILE_MAP = new MultiKeyMap<>();
    static {
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     TaskForce.class.getCanonicalName(),     "/taskforce/alliesTaskForces.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       TaskForce.class.getCanonicalName(),     "/taskforce/axisTaskForces.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Victory.class.getCanonicalName(),       "/victory/alliesVictory.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Victory.class.getCanonicalName(),       "/victory/axisVictory.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Aircraft.class.getCanonicalName(),      "/aircraft/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Aircraft.class.getCanonicalName(),      "/aircraft/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Port.class.getCanonicalName(),          "/ports/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Port.class.getCanonicalName(),          "/ports/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Airfield.class.getCanonicalName(),      "/airfields/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Airfield.class.getCanonicalName(),      "/airfields/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Region.class.getCanonicalName(),        "/maps/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Region.class.getCanonicalName(),        "/maps/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Ship.class.getCanonicalName(),          "/ships/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Ship.class.getCanonicalName(),          "/ships/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Squadron.class.getCanonicalName(),      "/squadrons/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Squadron.class.getCanonicalName(),      "/squadrons/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Allotment.class.getCanonicalName(),     "/squadrons/allotment/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Allotment.class.getCanonicalName(),     "/squadrons/allotment/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Deployment.class.getCanonicalName(),    "/squadrons/deployment/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Deployment.class.getCanonicalName(),    "/squadrons/deployment/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Minefield.class.getCanonicalName(),     "/minefield/alliesMinefields.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Minefield.class.getCanonicalName(),     "/minefield/axisMinefields.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     MinefieldZone.class.getCanonicalName(), "/minefields/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       MinefieldZone.class.getCanonicalName(), "/minefields/axis/");
    }

    private static final Map<Class<?>, String> FILE_MAP = new HashMap<>();
    static {
        FILE_MAP.put(Game.class, "/game.json");
        FILE_MAP.put(Scenario.class, "/summary.json");
    }

    private static final MultiKeyMap<String, String> DEFAULT_FILE_MAP = new MultiKeyMap<>();
    static {
        DEFAULT_FILE_MAP.put(Side.ALLIES.toString(), Victory.class.getSimpleName(), "victory/allies");
        DEFAULT_FILE_MAP.put(Side.AXIS.toString(),   Victory.class.getSimpleName(), "victory/axis");

    }

    private static final String DEFAULT_SAVED_GAME = "/defaultGame";

    private GameTitle gameTitle;

    @Getter
    @Setter
    private GameType type;

    @Setter
    private String scenario;

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
     * Determine if this is a new game or an existing one.
     *
     * @return True if the game is new. False otherwise.
     */
    public boolean isNew() {
        return type == GameType.NEW;
    }

    /**
     * Get a URL in the game title's directory. This URL maps to a file in the game title's directory. BombAlley for
     * example.
     *
     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @param name The name of the entity instance.
     * @return The entity URL.
     */
    public Optional<URL> getGameURL(final Side side, final Class<?> clazz, final String name) {
        String entityName = clazz.getCanonicalName();
        String fileName = gameTitle.getValue() + SIDE_FILE_MAP.get(side.toString(), entityName) + name;
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get a URL in the scenario directory. This URL maps to a file in the scenario directory.

     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @param name The name of the entity instance.
     * @return The entity URL.
     */
    public Optional<URL> getScenarioURL(final Side side, final Class<?> clazz, final String name) {
        String entityName = clazz.getCanonicalName();
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario + SIDE_FILE_MAP.get(side.toString(), entityName) + name;
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get a URL in the scenario directory. This URL maps to a file in the scenario directory.

     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The entity URL.
     */
    public Optional<URL> getScenarioURL(final Side side, final Class<?> clazz) {
        String entityName = clazz.getCanonicalName();
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario + SIDE_FILE_MAP.get(side.toString(), entityName);
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get a URL in the scenario directory. This URL maps to a file in the scenario directory.
     *
     * @param clazz The entity class.
     * @return The entity URL.
     */
    public Optional<URL> getScenarioURL(final Class<?> clazz) {
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario + FILE_MAP.get(clazz);
        log.debug("'{}' URL: '{}'", clazz.getSimpleName(), fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get the URL of the current game's scenario directory.
     *
     * @return The game's scenario directory URL.
     */
    public Optional<URL> getScenarioDirectory() {
        return Optional.ofNullable(getClass().getClassLoader().getResource(gameTitle.getValue() + Config.SCENARIO_DIRECTORY_NAME));
    }

    /**
     * Get the scenario directory path.
     *
     * @return The game's scenario directory path.
     */
    public String getScenarioDirectoryNameName() {
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario;
        log.debug("Scenario URL: '{}'", fileName);
        return fileName;
    }

    /**
     * Get the default URL. This URL maps to the default folder of the game for the given entity.
     *
     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The default URL.
     */
    public Optional<URL> getDefaultURL(final Side side, final Class<?> clazz) {
        String entityName = clazz.getSimpleName();
        String fileName = DEFAULT_FILE_MAP.get(side.toString(), entityName) + "/default.json";
        log.debug("Default '{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get the saved URL of the given entity. This URL maps to a file in the current saved game directory.
     *
     * @param side The side ALLIES of AXIS.
     * @param clazz The entity class.
     * @param name The name of the entity instance.
     * @return The entity URL.
     */
    public Optional<URL> getSavedURL(final Side side, final Class<?> clazz, final String name) {
        String fileName = getSavedFileName(side, clazz, name);
        Path path = Paths.get(fileName);
        try {
            return Optional.of(path.toUri().toURL());
        } catch (MalformedURLException ex) {
            log.error("Bad url '{}'", path);
            return Optional.empty();
        }
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

     /** Get the saved URL of the given entity. This URL maps to a file in the current saved game directory.
      *
      * @param clazz The entity class.
      * @return The URL of the entity.
     */
    public Optional<URL> getSavedURL(final Class<?> clazz) {
        String fileName = getSavedFileName(clazz);
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
     * @param name The name of the entity instance.
     * @return The file path of the entity's saved game.
     */
    public String getSavedFileName(final Side side, final Class<?> clazz, final String name) {
        String entityName = clazz.getCanonicalName();
        String fileName = SAVED_GAME_DIRECTORY + scenario + savedGameName + SIDE_FILE_MAP.get(side.toString(), entityName) + name;
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return fileName;
    }

    /**
     * Get the saved file path of the given entity. This path maps to a file in the current saved game directory.
     *
     * @param side The side ALLIES or AXIS.
     * @param clazz The entity class.
     * @return The file path of the entity's saved game.
     */
    public String getSavedFileName(final Side side, final Class<?> clazz) {
        String entityName = clazz.getCanonicalName();
        String fileName = SAVED_GAME_DIRECTORY + scenario + savedGameName + SIDE_FILE_MAP.get(side.toString(), entityName);
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return fileName;
    }

    /**
     * Get the saved file path of the given entity. This path maps to a file in the current saved game directory.
     *
     * @param clazz The entity class.
     * @return The file path of the entity's saved game.
     */
    public String getSavedFileName(final Class<?> clazz) {
        String fileName = SAVED_GAME_DIRECTORY + scenario + savedGameName + FILE_MAP.get(clazz);
        log.debug("'{}' URL: '{}'", clazz.getSimpleName(), fileName);
        return fileName;
    }

    /**
     * Get the given scenario's summary file.
     *
     * @param scenarioDirectory The scenario directory.
     * @return The path to the scenario summary file.
     */
    public Path getScenarioSummary(final String scenarioDirectory) {
        return Paths.get(scenarioDirectory, SUMMARY_FILE_NAME);

    }
}
