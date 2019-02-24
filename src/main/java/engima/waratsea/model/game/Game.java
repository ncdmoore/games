package engima.waratsea.model.game;

import com.google.inject.Inject;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.scenario.ScenarioLoader;
import engima.waratsea.model.victory.Victory;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;

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
    @Setter
    private String name;

    @Getter
    private Side humanSide;

    @Getter
    private final Player computerPlayer;

    @Getter
    private final Player humanPlayer;

    private final Map<Side, Player> players = new HashMap<>();

    private final ScenarioLoader scenarioLoader;
    private final GameMap gameMap;
    private final Victory gameVictory;

    @Getter
    @Setter
    private Scenario scenario;

    /**
     * The constructor for the game.
     *
     * @param humanPlayer The human player.
     * @param computerPlayer The computer player.
     * @param gameMap The game map.
     * @param gameVictory The game victory conditions and status.
     * @param scenarioLoader Loads player task forces.
     */
    @Inject
    public Game(@Named("Human") final Player humanPlayer,
                @Named("Computer") final Player computerPlayer,
                final GameMap gameMap,
                final Victory gameVictory,
                final ScenarioLoader scenarioLoader) {
        this.humanPlayer = humanPlayer;
        this.computerPlayer = computerPlayer;
        this.gameMap = gameMap;
        this.gameVictory = gameVictory;
        this.scenarioLoader = scenarioLoader;
    }

    /**
     * Initialize the scenario summary data.
     *
     * @return List of scenarios.
     * @throws ScenarioException Indicates the scenario summary data could not be loaded.
     */
    public List<Scenario> initScenarios() throws ScenarioException {                                                    // New Game Step 1.
        return scenarioLoader.loadSummaries();
    }

    /**
     * Sets the sides of the two players of the game.
     *
     * @param side The human player humanSide.
     */
    public void setHumanSide(final Side side) {                                                                         // New Game Step 2.
        log.info("Human side: {}", side);

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
    public void startNew() throws ScenarioException, MapException, VictoryException {                                      // New Game Step 3.
        GameEvent.init();

        loadGameMap();
        loadGameVictory();
        buildAssets();

        save();              //Save the default game.
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
     * Save the game.
     */
    private void save() {
        gameVictory.save(scenario);
    }
}
