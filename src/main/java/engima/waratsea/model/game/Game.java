package engima.waratsea.model.game;

import com.google.inject.Inject;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.game.event.scenario.ScenarioEvent;
import engima.waratsea.model.game.event.scenario.ScenarioEventTypes;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioDAO;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.victory.VictoryException;
import engima.waratsea.model.weather.Weather;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class represents the game. It contains the game rules, game players etc.
 */
@Slf4j
@Singleton
public class Game implements PersistentData<GameData> {
    @Getter private final Player computerPlayer;
    @Getter private final Player humanPlayer;
    @Getter private final Player neutralPlayer;
    @Getter private final Weather weather;
    @Getter private final Turn turn;

    private final Map<Side, Player> playerMap = new HashMap<>();
    private final Resource resource;
    private final ScenarioDAO scenarioDAO;
    private final GameDAO gameDAO;
    private final GameMap gameMap;

    private final AtomicInteger airMissionId;

    @Getter private Side humanSide;
    @Getter private Scenario scenario; // The selected scenario.

    /**
     * Constructor called by guice.
     *
     * @param turn The game turn.
     * @param weather The game weather.
     * @param computerPlayer The computer player.
     * @param humanPlayer The human player.
     * @param neutralPlayer The neutral player.
     * @param resource The game configuration.
     * @param scenarioDAO  The scenario data abstraction object.
     * @param gameDAO The game data abstraction object.
     * @param gameMap The game map.
     */
    //CHECKSTYLE:OFF
    @Inject
    public Game(final Turn turn,
                final Weather weather,
                final @Named("Computer") Player computerPlayer,
                final @Named("Human") Player humanPlayer,
                final @Named("Neutral") Player neutralPlayer,
                final Resource resource,
                final ScenarioDAO scenarioDAO,
                final GameDAO gameDAO,
                final GameMap gameMap) {
        //CHECKSTYLE:ON

        final int startingAirMissionId = 100;

        this.turn = turn;
        this.weather = weather;
        this.computerPlayer = computerPlayer;
        this.humanPlayer = humanPlayer;
        this.neutralPlayer = neutralPlayer;
        this.resource = resource;
        this.scenarioDAO = scenarioDAO;
        this.gameDAO = gameDAO;
        this.gameMap = gameMap;
        this.airMissionId = new AtomicInteger(startingAirMissionId);
    }

    /**
     * Initialize the game from saved game data.
     *
     * @param data The persistent game data.
     */
    public void init(final GameData data) {
        resource.setType(GameType.EXISTING);
        setScenario(data.getScenario());
        setHumanSide(data.getHumanSide());
        getTurn().init(data.getTurn());
        getWeather().setCurrent(data.getWeather());
        setSavedGameName(data.getSavedGameName());
        airMissionId.set(data.getAirMissionId());
    }

    /**
     * Get all of the game persistent data.
     *
     * @return The game's persistent data.
     */
    public GameData getData() {
        GameData data = new GameData();
        data.setAirMissionId(airMissionId.get());
        data.setHumanSide(humanSide);
        data.setScenario(scenario);
        data.setTurn(turn.getData());
        data.setWeather(weather.getCurrent());
        data.setSavedGameName(resource.getSavedGameName());
        return data;
    }

    /**
     * Get the next air mission id.
     *
     * @return The next air mission id.
     */
    public int getAirMissionId() {
        return airMissionId.getAndIncrement();
    }

    /**
     * Get the player for the given side.
     *
     * @param side The side: ALLIES or AXIS.
     * @return The corresponding player for the given side.
     */
    public Player getPlayer(final Side side) {
        return playerMap.get(side);
    }

    /**
     * Set the game type as a new game.
     */
    public void setNew() {
        resource.setType(GameType.NEW);
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
        resource.setScenario(scenario.getName());
    }

    /**
     * Sets the sides of the two players of the game.
     *
     * @param side The human player humanSide.
     */
    public void setHumanSide(final Side side) {                                                                         // New Game Step 3.
        humanSide = side;                                                                                               // Saved Game Step 3.
        humanPlayer.setSide(humanSide);
        computerPlayer.setSide(humanSide.opposite());

        playerMap.put(side, humanPlayer);
        playerMap.put(side.opposite(), computerPlayer);
        playerMap.put(Side.NEUTRAL, neutralPlayer);
    }

    /**
     * Sets the name of the saved game.
     *
     * @param savedGameName The saved game name.
     */
    public void setSavedGameName(final String savedGameName) {
        resource.setSavedGameName(savedGameName);
    }

    /**
     * Initialize the game data for both players for a new game.
     *
     * @throws ScenarioException Indicates the scenario data could not be loaded.
     * @throws MapException Indicates the map data could not be loaded.
     * @throws VictoryException Indicates the victory data could not be loaded.
     * @throws SquadronException Indicates the squadron data could not be loaded.
     */
    public void startNew() throws ScenarioException, MapException, VictoryException, SquadronException {                // New Game Step 4.
        init();            // Initialize the game.

        loadGameMap();     // Loads airfields and ports. They are part of the map.
        loadGameVictory();
        setNations();
        loadSquadrons();   // Loads the squadrons from the allotment.

        buildAssets();
        deployAssets();

        buildViews();

        turn.start(scenario);
        weather.start(scenario);
    }

    /**
     * Load a save game.
     *
     * @throws ScenarioException indicates that the task forces could not be loaded.
     * @throws MapException indicates that the map could not be loaded.
     * @throws VictoryException indicates that the victory conditions could not be loaded.
     */
    public void startExisting() throws ScenarioException, MapException, VictoryException {                              // Saved Game Step 4.
        init();            // Initialize the game.

        loadGameMap();     // Loads airfields and ports. They are part of the  map.
        loadGameVictory();
        setNations();

        // No need to load squadrons. For saved games they are loaded with the airfields
        // as part of the loadGameMap method above. This is similar to how carrier
        // squadrons are loaded.

        buildAssets();
        setSquadrons();
        // No need to deploy assets as this has already been done.

        buildViews();
    }

    /**
     * Save the given game.
     *
     * @param savedGameName The name of the saved game.
     */
    public void save(final String savedGameName) {
        resource.setSavedGameName(savedGameName);
        gameDAO.save(this);
        humanPlayer.saveVictory(scenario);
        humanPlayer.saveAssets(scenario);
        computerPlayer.saveVictory(scenario);
        computerPlayer.saveAssets(scenario);
        neutralPlayer.saveAssets(scenario);
    }

    /**
     * Save the default game.
     */
    public void save() {
        resource.setSavedGameName(Resource.DEFAULT_SAVED_GAME);
        gameDAO.save(this);
        humanPlayer.saveVictory(scenario);
        humanPlayer.saveAssets(scenario);
        computerPlayer.saveVictory(scenario);
        computerPlayer.saveAssets(scenario);
        neutralPlayer.saveAssets(scenario);
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
        neutralPlayer.setNations();
    }

    /**
     * Load the player's squadrons. Only called for new games.
     *
     * @throws SquadronException if the squadrons cannot be loaded.
     **/
    private void loadSquadrons() throws SquadronException {
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
     * Load the player's assets.
     *
     * @throws ScenarioException Indicates the task forces could not be loaded.
     */
    private void buildAssets() throws ScenarioException {
        humanPlayer.buildAssets(scenario);
        computerPlayer.buildAssets(scenario);
        neutralPlayer.buildAssets(scenario);
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

    /**
     * Load the player's view of the enemy assets.
     **/
    private void buildViews() {
        humanPlayer.buildViews(computerPlayer);
        computerPlayer.buildViews(humanPlayer);
    }

    /**
     * Initialize a game.
     */
    private void init() {
        GameEvent.init();

        ScenarioEvent event = new ScenarioEvent(ScenarioEventTypes.BOOT);
        event.fire();
    }
}
