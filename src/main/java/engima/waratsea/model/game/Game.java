package engima.waratsea.model.game;

import com.google.inject.Inject;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.scenario.ScenarioDAO;
import engima.waratsea.model.victory.Victory;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the game. It contains the game rules, game players etc.
 */
@Slf4j
@Singleton
public class Game {
    @Getter
    @Inject
    @Named("Computer")
    @SuppressWarnings("unused")
    private Player computerPlayer;

    @Getter
    @Inject
    @Named("Human")
    @SuppressWarnings("unused")
    private Player humanPlayer;

    @Inject
    @SuppressWarnings("unused")
    private  Config config;

    @Inject
    @SuppressWarnings("unused")
    private ScenarioDAO scenarioDAO;

    @Inject
    @SuppressWarnings("unused")
    private GameDAO gameDAO;

    @Inject
    @SuppressWarnings("unused")
    private  GameMap gameMap;

    @Inject
    @SuppressWarnings("unused")
    private  Victory gameVictory;

    @Getter
    private Side humanSide;

    @Getter
    private Scenario scenario; // The selected scenario.

    private  Map<Side, Player> players = new HashMap<>();


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
     * Set the game's selected scenario.
     *
     * @param selectedScenario The selected scenario.
     */
    public void setScenario(final Scenario selectedScenario) {                                                          // New Game Step 2.
        scenario = selectedScenario;
        config.setScenario(scenario.getName());
    }

    /**
     * Sets the sides of the two players of the game.
     *
     * @param side The human player humanSide.
     */
    public void setHumanSide(final Side side) {                                                                         // New Game Step 2.
        log.debug("Human side: {}", side);

        humanSide = side;
        humanPlayer.setSide(humanSide);
        computerPlayer.setSide(humanSide.opposite());
        players.put(side, humanPlayer);
        players.put(side.opposite(), computerPlayer);
    }

    /**
     * Get a player given the player's side.
     *
     * @param side The side ALLIES or AXIS.
     * @return The player that corresponds to the given side.
     */
    public Player getPlayer(final Side side) {
        return players.get(side);
    }

    /**
     * Initialize the game data for both players for a new game.
     *
     * @throws ScenarioException Indicates the scenario data could not be loaded.
     * @throws MapException Indicates the map data could not be loaded.
     * @throws VictoryException Indicates the victory data could not be loaded.
     */
    public void startNew() throws ScenarioException, MapException, VictoryException {                                   // New Game Step 3.
        GameEvent.init();

        loadGameMap();
        loadGameVictory();
        buildAssets();
        deployAssets();

        save();              //Save the default game.
    }

    /**
     * Set the saved games scenario name. This is needed in order to load the saved game files.
     *
     * @param scenarioName The saved game's scenario name.
     */
    public void setScenarioName(final String scenarioName) {                                                            // Saved Game Step 1.
        config.setScenario(scenarioName);
    }

    /**
     * Set the saved game name. This is needed in order to load the saved game files.
     *
     * @param savedGameName The name of the saved game.
     */
    public void setSavedGameName(final String savedGameName) {                                                          // Saved Game Step 2.
        config.setSavedGameName(savedGameName);
    }

    /**
     * Load a save game.
     *
     * @throws GameException indicates the main game data could not be loaded.
     * @throws MapException indicates that the map could not be loaded.
     * @throws VictoryException indicates that the victory conditions could not be loaded.
     * @throws ScenarioException indicates that the task forces could not be loaded.
     */
    public void startExisting() throws  GameException, MapException, VictoryException, ScenarioException {              // Saved Game Step 3.
        GameEvent.init();

        loadSavedGame();

        loadGameMap();
        loadGameVictory();
        buildAssets();
    }

    /**
     * Load a saved game.
     *
     * @throws GameException indicates that the game could not be loaded.
     */
    private void loadSavedGame() throws GameException {
        GameData data = gameDAO.load();
        setHumanSide(data.getHumanSide());
        setScenario(data.getScenario());
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
        gameVictory.load(scenario);
        scenario.setObjectives(gameVictory.getObjectives(humanPlayer.getSide()));
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
     */
    private void deployAssets() {
        computerPlayer.deployAssets(scenario);
    }

    /**
     * Save the game.
     */
    private void save() {
        gameDAO.save(getData());
        gameVictory.save(scenario);
        humanPlayer.saveAssets(scenario);
        computerPlayer.saveAssets(scenario);
    }

    /**
     * Get all of the game persistent data.
     *
     * @return The game's persistent data.
     */
    private GameData getData() {
        GameData data = new GameData();
        data.setHumanSide(humanSide);
        data.setScenario(scenario);
        return data;
    }
}
