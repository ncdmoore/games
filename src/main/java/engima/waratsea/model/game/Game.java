package engima.waratsea.model.game;

import com.google.inject.Inject;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.scenario.ScenarioDAO;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * This class represents the game. It contains the game rules, game players etc.
 */
@Slf4j
@Singleton
public class Game implements PersistentData<GameData> {
    @Getter
    private Player computerPlayer;

    @Getter
    private Player humanPlayer;

    private Resource config;
    private ScenarioDAO scenarioDAO;
    private GameDAO gameDAO;
    private GameMap gameMap;

    @Getter
    private Side humanSide;

    @Getter
    private Scenario scenario; // The selected scenario.

    /**
     * Constructor called by guice.
     *
     * @param computerPlayer The computer player.
     * @param humanPlayer The human player.
     * @param config The game configuration.
     * @param scenarioDAO  The scenario data abstraction object.
     * @param gameDAO The game data abstraction object.
     * @param gameMap The game map.
     */
    @Inject
    public Game(final @Named("Computer") Player computerPlayer,
                final @Named("Human") Player humanPlayer,
                final Resource config,
                final ScenarioDAO scenarioDAO,
                final GameDAO gameDAO,
                final GameMap gameMap) {
        this.computerPlayer = computerPlayer;
        this.humanPlayer = humanPlayer;
        this.config = config;
        this.scenarioDAO = scenarioDAO;
        this.gameDAO = gameDAO;
        this.gameMap = gameMap;
    }

    /**
     * Get all of the game persistent data.
     *
     * @return The game's persistent data.
     */
    public GameData getData() {
        GameData data = new GameData();
        data.setHumanSide(humanSide);
        data.setScenario(scenario);
        data.setSavedGameName(config.getSavedGameName());
        return data;
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * Set the game type as a new game.
     */
    public void setNew() {
        config.setType(GameType.NEW);
    }

    /**
     * Set the game type as an existing game.
     */
    public void setExisting() {
        config.setType(GameType.EXISTING);
    }

    /**
     * Initialize the scenario summary data.
     *
     * @return List of scenarios.
     * @throws ScenarioException Indicates the scenario summary data could not be loaded.
     */
    public List<Scenario> initScenarios() throws ScenarioException {                                                    // New Game Step 1.
        return scenarioDAO.load();
    }

    /**
     * Initialize the saved game data.
     *
     * @return List of game data.
     * @throws ScenarioException Indicates the game data could not be loaded.
     */
    public List<GameData> initGames() throws ScenarioException {                                                        // Saved Game Step 1.
        return gameDAO.load();
    }

    /**
     * Set the game's selected scenario.
     *
     * @param selectedScenario The selected scenario.
     */
    public void setScenario(final Scenario selectedScenario) {                                                          // New Game Step 2.
        scenario = selectedScenario;                                                                                    // Saved Game Step 2.
        config.setScenario(scenario.getName());
    }

    /**
     * Sets the sides of the two players of the game.
     *
     * @param side The human player humanSide.
     */
    public void setHumanSide(final Side side) {                                                                         // New Game Step 3.
        log.debug("Human side: {}", side);                                                                              // Saved Game Step 3.

        humanSide = side;
        humanPlayer.setSide(humanSide);
        computerPlayer.setSide(humanSide.opposite());
    }

    /**
     * Sets the name of the saved game.
     *
     * @param savedGameName The saved game name.
     */
    public void setSavedGameName(final String savedGameName) {
        config.setSavedGameName(savedGameName);
    }

    /**
     * Initialize the game data for both players for a new game.
     *
     * @throws ScenarioException Indicates the scenario data could not be loaded.
     * @throws MapException Indicates the map data could not be loaded.
     * @throws VictoryException Indicates the victory data could not be loaded.
     */
    public void startNew() throws ScenarioException, MapException, VictoryException {                                   // New Game Step 4.
        GameEvent.init();

        loadGameMap();     // Loads airfields and ports. They are part of the map.
        loadGameVictory();
        setNations();
        loadSquadrons();   // Loads the squadrons from the allotment.

        buildAssets();
        deployAssets();
    }

    /**
     * Load a save game.
     *
     * @throws ScenarioException indicates that the task forces could not be loaded.
     * @throws MapException indicates that the map could not be loaded.
     * @throws VictoryException indicates that the victory conditions could not be loaded.
     */
    public void startExisting() throws ScenarioException, MapException, VictoryException {                              // Saved Game Step 4.
        GameEvent.init();

        loadGameMap();     // Loads airfields and ports. They are part of the  map.
        loadGameVictory();
        setNations();

        // No need to load squadrons. For saved games they are loaded with the airfields
        // as part of the loadGameMap method above. This is similar to how carrier
        // squadrons are loaded.

        buildAssets();
        setSquadrons();

        // No need to deploy assets as this has already been done.
    }

    /**
     * Save the given game.
     *
     * @param savedGameName The name of the saved game.
     */
    public void save(final String savedGameName) {
        config.setSavedGameName(savedGameName);
        gameDAO.save(this);
        humanPlayer.saveVictory(scenario);
        humanPlayer.saveAssets(scenario);
        computerPlayer.saveVictory(scenario);
        computerPlayer.saveAssets(scenario);
    }

    /**
     * Save the default game.
     */
    public void save() {
        config.setSavedGameName(Resource.DEFAULT_SAVED_GAME);
        gameDAO.save(this);
        humanPlayer.saveVictory(scenario);
        humanPlayer.saveAssets(scenario);
        computerPlayer.saveVictory(scenario);
        computerPlayer.saveAssets(scenario);
    }

    /**
     * Load the game map.
     *
     * @throws MapException Indicates the game map could not be loaded.
     */
    private void loadGameMap() throws MapException {
        gameMap.load(scenario);
    }

    /**
     * Load the game gameVictory. Also sets the scenario victory objective for the human player.
     *
     * @throws VictoryException Indicates the game gameVictory could not be loaded.
     */
    private void loadGameVictory() throws VictoryException {
        humanPlayer.buildVictory(scenario);
        computerPlayer.buildVictory(scenario);
    }

    /**
     * Set the player's nations.
     */
    private void setNations() {
        humanPlayer.setNations();
        computerPlayer.setNations();
    }

    /**
     * Load the player's squadrons. Only called for new games.
     **/
    private void loadSquadrons()  {
        humanPlayer.loadSquadrons(scenario);
        computerPlayer.loadSquadrons(scenario);
    }

    /**
     * set the player's squadrons. Only called for existing games.
     */
    private void setSquadrons() {
        humanPlayer.setSquadrons();
        computerPlayer.setSquadrons();
    }

    /**
     * Load the task forces.
     *
     * @throws ScenarioException Indicates the task forces could not be loaded.
     */
    private void buildAssets() throws ScenarioException {
        humanPlayer.buildAssets(scenario);
        computerPlayer.buildAssets(scenario);
    }

    /**
     * Deploy the computer player's assets.
     *
     * @throws ScenarioException Indicates the task forces could not be loaded.
     */
    private void deployAssets() throws ScenarioException {
        humanPlayer.deployAssets(scenario);
        computerPlayer.deployAssets(scenario);
    }
}
