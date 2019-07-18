package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.flotilla.MotorTorpedoBoatFlotilla;
import engima.waratsea.model.flotilla.SubmarineFlotilla;
import engima.waratsea.model.flotilla.deployment.FlotillaDeployment;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.deployment.MinefieldDeployment;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.deployment.SquadronDeployment;
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

    private static final MultiKeyMap<String, String> SIDE_FILE_MAP = new MultiKeyMap<>();
    static {
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     TaskForce.class.getSimpleName(),                "/taskforce/alliesTaskForces.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       TaskForce.class.getSimpleName(),                "/taskforce/axisTaskForces.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     SubmarineFlotilla.class.getSimpleName(),        "/flotilla/alliesSubmarineFlotilla.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       SubmarineFlotilla.class.getSimpleName(),        "/flotilla/axisSubmarineFlotilla.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     MotorTorpedoBoatFlotilla.class.getSimpleName(), "/flotilla/alliesMTBFlotilla.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       MotorTorpedoBoatFlotilla.class.getSimpleName(), "/flotilla/axisMTBFlotilla.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     FlotillaDeployment.class.getSimpleName(),       "/flotilla/deployment/alliesFlotilla.json/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       FlotillaDeployment.class.getSimpleName(),       "/flotilla/deployment/axisFlotilla.json/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Victory.class.getSimpleName(),                  "/victory/alliesVictory.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Victory.class.getSimpleName(),                  "/victory/axisVictory.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Aircraft.class.getSimpleName(),                 "/aircraft/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Aircraft.class.getSimpleName(),                 "/aircraft/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Port.class.getSimpleName(),                     "/ports/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Port.class.getSimpleName(),                     "/ports/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Airfield.class.getSimpleName(),                 "/airfields/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Airfield.class.getSimpleName(),                 "/airfields/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Region.class.getSimpleName(),                   "/maps/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Region.class.getSimpleName(),                   "/maps/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Ship.class.getSimpleName(),                     "/ships/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Ship.class.getSimpleName(),                     "/ships/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Squadron.class.getSimpleName(),                 "/squadrons/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Squadron.class.getSimpleName(),                 "/squadrons/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Allotment.class.getSimpleName(),                "/squadrons/allotment/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Allotment.class.getSimpleName(),                "/squadrons/allotment/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     SquadronDeployment.class.getSimpleName(),       "/squadrons/deployment/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       SquadronDeployment.class.getSimpleName(),       "/squadrons/deployment/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     Minefield.class.getSimpleName(),                "/minefield/alliesMinefields.json");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       Minefield.class.getSimpleName(),                "/minefield/axisMinefields.json");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     MinefieldZone.class.getSimpleName(),            "/minefields/allies/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       MinefieldZone.class.getSimpleName(),            "/minefields/axis/");
        SIDE_FILE_MAP.put(Side.ALLIES.toString(),     MinefieldDeployment.class.getSimpleName(),      "/minefield/deployment/alliesMinefield.json/");
        SIDE_FILE_MAP.put(Side.AXIS.toString(),       MinefieldDeployment.class.getSimpleName(),      "/minefield/deployment/axisMinefield.json/");
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

    public static final String DEFAULT_SAVED_GAME = "/defaultGame";

    private final GameTitle gameTitle;
    private final String savedGameDirectory;

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
        this.savedGameDirectory = System.getProperty("user.home") + "/WW2atSea/SavedGames/" + gameTitle.getValue() + "/";
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
        String entityName = clazz.getSimpleName();
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
        String entityName = clazz.getSimpleName();
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
        String entityName = clazz.getSimpleName();
        String fileName = gameTitle.getValue() + SCENARIO_DIRECTORY_NAME + "/" + scenario + SIDE_FILE_MAP.get(side.toString(), entityName);
        log.debug("'{}' URL: '{}'", entityName, fileName);
        return Optional.ofNullable(getClass().getClassLoader().getResource(fileName));
    }

    /**
     * Get the URL of the current game's scenario directory.
     *
     * @return The game's scenario directory URL.
     */
    public Optional<URL> getScenarioDirectory() {
        return Optional
                .ofNullable(getClass()
                .getClassLoader()
                .getResource(gameTitle.getValue() + Config.SCENARIO_DIRECTORY_NAME));
    }

    /**
     * Get the URL of the saved game directory.
     *
     * @return The saved game directory URL.
     */
    public Optional<URL> getSavedDirectory() {
        Path path = Paths.get(savedGameDirectory);
        try {
            return Optional.of(path.toUri().toURL());
        } catch (MalformedURLException ex) {
            log.error("Bad url '{}'", path);
            return Optional.empty();
        }
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
        String entityName = clazz.getSimpleName();
        String fileName = savedGameDirectory + scenario + savedGameName + SIDE_FILE_MAP.get(side.toString(), entityName) + name;
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
        String entityName = clazz.getSimpleName();
        String fileName = savedGameDirectory + scenario + savedGameName + SIDE_FILE_MAP.get(side.toString(), entityName);
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
        String fileName = savedGameDirectory + scenario + savedGameName + FILE_MAP.get(clazz);
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
