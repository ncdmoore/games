package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.airfield.AirfieldViewDAO;
import engima.waratsea.model.enemy.views.port.PortView;
import engima.waratsea.model.enemy.views.port.PortViewDAO;
import engima.waratsea.model.enemy.views.taskForce.TaskForceView;
import engima.waratsea.model.enemy.views.taskForce.TaskForceViewDAO;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaAI;
import engima.waratsea.model.flotilla.FlotillaDAO;
import engima.waratsea.model.flotilla.FlotillaType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.MinefieldAI;
import engima.waratsea.model.minefield.MinefieldDAO;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronAI;
import engima.waratsea.model.squadron.SquadronDAO;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetDAO;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceDAO;
import engima.waratsea.model.taskForce.mission.SeaMissionType;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryDAO;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the computer player in the game.
 */
@Slf4j
public class ComputerPlayer implements Player {

    private final GameMap gameMap;
    private final VictoryDAO victoryDAO;
    private final TaskForceDAO taskForceDAO;
    private final TaskForceViewDAO taskForceViewDAO;
    private final FlotillaDAO flotillaDAO;
    private final AirfieldDAO airfieldDAO;
    private final AirfieldViewDAO airfieldViewDAO;
    private final PortDAO portDAO;
    private final PortViewDAO portViewDAO;
    private final SquadronDAO aviationPlant;
    private final MinefieldDAO minefieldDAO;
    private final TargetDAO targetDAO;

    private final FlotillaAI flotillaAI;
    private final SquadronAI squadronAI;
    private final MinefieldAI minefieldAI;

    @Getter private VictoryConditions victoryConditions;

    @Getter @Setter private Side side;
    @Getter private Set<Nation> nations;
    @Getter private List<TaskForce> taskForces;
    @Getter private List<TaskForceView> enemyTaskForces;
    @Getter private List<Airfield> airfields;
    @Getter private List<AirfieldView> enemyAirfields;
    @Getter private List<Port> ports;
    @Getter private List<PortView> enemyPorts;
    @Getter private List<Minefield> minefields;

    private Map<String, TaskForce> taskForceMap;
    private Map<String, Airfield> airfieldMap;
    private final Map<String, Airbase> airbaseMap = new HashMap<>();
    private Map<String, Port> portMap;

    private Map<String, AirfieldView> enemyAirfieldMap;
    private Map<String, PortView> enemyPortMap;
    private Map<String, TaskForceView> enemyTaskForceMap;

    private final Map<FlotillaType, List<Flotilla>> flotillas = new HashMap<>();
    private final Map<Nation, List<Squadron>> squadrons = new HashMap<>();
    private final Map<AirMissionType, Function<Nation, List<Target>>> airTargetMap = new HashMap<>();
    private final Map<SeaMissionType, Supplier<List<Target>>> seaTargetMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param victoryDAO Loads the computer player's victory conditions.
     * @param taskForceDAO Loads task force data.
     * @param taskForceViewDAO Loads the task force view data.
     * @param flotillaDAO Loads flotilla data.
     * @param airfieldDAO Loads airfield data.
     * @param airfieldViewDAO Loads enemy airfield view data.
     * @param portDAO Loads port data.
     * @param portViewDAO Loads enemy port view data.
     * @param minefieldDAO Loads minefield data.
     * @param aviationPlant Loads squadron data.
     * @param targetDAO Creates targets.
     * @param flotillaAI  flotilla AI.
     * @param squadronAI squadron AI.
     * @param minefieldAI minefield AI.
     */
    //CHECKSTYLE:OFF
    @Inject
    public ComputerPlayer(final GameMap gameMap,
                          final VictoryDAO victoryDAO,
                          final TaskForceDAO taskForceDAO,
                          final TaskForceViewDAO taskForceViewDAO,
                          final FlotillaDAO flotillaDAO,
                          final AirfieldDAO airfieldDAO,
                          final AirfieldViewDAO airfieldViewDAO,
                          final PortDAO portDAO,
                          final PortViewDAO portViewDAO,
                          final MinefieldDAO minefieldDAO,
                          final SquadronDAO aviationPlant,
                          final TargetDAO targetDAO,
                          final FlotillaAI flotillaAI,
                          final SquadronAI squadronAI,
                          final MinefieldAI minefieldAI) {
        //CHECKSTYLE:ON

        this.gameMap = gameMap;
        this.victoryDAO = victoryDAO;
        this.taskForceViewDAO = taskForceViewDAO;
        this.taskForceDAO = taskForceDAO;
        this.flotillaDAO = flotillaDAO;
        this.airfieldDAO = airfieldDAO;
        this.airfieldViewDAO = airfieldViewDAO;
        this.portDAO = portDAO;
        this.portViewDAO = portViewDAO;
        this.aviationPlant = aviationPlant;
        this.minefieldDAO = minefieldDAO;
        this.targetDAO = targetDAO;

        this.flotillaAI = flotillaAI;
        this.squadronAI = squadronAI;
        this.minefieldAI = minefieldAI;

        airTargetMap.put(AirMissionType.DISTANT_CAP, nation -> getFriendlyTaskForceTargets());
        airTargetMap.put(AirMissionType.FERRY, this::getFriendlyAirbaseTargets);
        airTargetMap.put(AirMissionType.LAND_STRIKE, nation -> getEnemyAirfieldTargets());
        airTargetMap.put(AirMissionType.SWEEP_AIRFIELD, nation -> getEnemyAirfieldTargets());
        airTargetMap.put(AirMissionType.NAVAL_PORT_STRIKE, nation -> getEnemyPortTargets());
        airTargetMap.put(AirMissionType.SWEEP_PORT, nation -> getEnemyPortTargets());
        airTargetMap.put(AirMissionType.NAVAL_TASK_FORCE_STRIKE, nation -> getEnemyTaskForceTargets());

        seaTargetMap.put(SeaMissionType.INTERCEPT, this::getEnemyTaskForceTargets);
    }

    /**
     * This gets the nations of the player based on the type of squadron location.
     *
     * @param type The type of squadrons: LAND or SEA.
     * @return A set of the player's nations: BRITISH, ITALIAN, etc...
     */
    @Override
    public Set<Nation> getSquadronNations(final SquadronLocationType type) {
        return type == SquadronLocationType.LAND ? nations : getTaskForceSquadronNations();
    }

    /**
     * Set the player's nations.
     */
    public void setNations() {
        nations = gameMap.getNations(side);
    }

    /**
     * Build the player's victory conditions.
     *
     * @param scenario The selected scenario.
     * @throws VictoryException is thrown if the victory conditions cannot be loaded.
     */
    @Override
    public void buildVictory(final Scenario scenario) throws VictoryException {
        victoryConditions = victoryDAO.load(scenario, side);
    }

    /**
     * This sets the player's task forces.
     * @param scenario The selected scenario.
     * @throws ScenarioException If the scenario data cannot be properly loaded.
     */
    @Override
    public void buildAssets(final Scenario scenario) throws ScenarioException {
        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = gameMap.getAirfields(side);

        airfieldMap = airfields
                .stream()
                .collect(Collectors.toMap(Airfield::getName, airfield -> airfield));

        airbaseMap.putAll(airfieldMap);   // All airfields are airbases.

        ports = gameMap.getPorts(side);

        portMap = ports
                .stream()
                .collect(Collectors.toMap(Port::getName, port -> port));

        minefields = gameMap.getMinefields(side);

        loadTaskForces(scenario);
        loadFlotillas(scenario);
    }

    /**
     * This builds the player's list of known targets.
     *
     * @param opposingPlayer The opposing player.
     */
    @Override
    public void buildViews(final Player opposingPlayer) {
        enemyPorts = portViewDAO.load(gameMap.getPorts(side.opposite()));
        enemyPortMap = enemyPorts
                .stream()
                .collect(Collectors.toMap(PortView::getName, pv -> pv));

        enemyAirfields = airfieldViewDAO.load(gameMap.getAirfields(side.opposite()));
        enemyAirfieldMap = enemyAirfields
                .stream()
                .collect(Collectors.toMap(AirfieldView::getName, av -> av));

        enemyTaskForces = taskForceViewDAO.load(opposingPlayer.getTaskForces());
        enemyTaskForceMap = enemyTaskForces
                .stream()
                .collect(Collectors.toMap(TaskForceView::getName, tfv -> tfv));
    }

    /**
     * Save the victory conditions.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveVictory(final Scenario scenario) {
        victoryDAO.save(scenario, side, victoryConditions);
    }

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        taskForceDAO.save(scenario, side, taskForces);
        FlotillaType.stream().forEach(flotillaType -> flotillaDAO.save(scenario, side, flotillas.get(flotillaType)));
        portDAO.save(scenario, side, ports);
        airfieldDAO.save(scenario, side, airfields);
        minefieldDAO.save(scenario, side, minefields);

        airfieldViewDAO.save(scenario, side, enemyAirfields);
        portViewDAO.save(scenario, side, enemyPorts);
        taskForceViewDAO.save(scenario, side, enemyTaskForces);
    }

    /**
     * Determines if the player has any flotilla's of the given type.
     *
     * @param flotillaType The flotilla type: SUBMARINE or MTB.
     * @return True if the player has a flotilla of the given type.
     */
    @Override
    public boolean hasFlotilla(final FlotillaType flotillaType) {
        return !flotillas.get(flotillaType).isEmpty();
    }

    /**
     * This gets the player's flotillas.
     *
     * @param flotillaType The type of flotilla: SUBMARINE or MTB.
     * @return The player's flotillas.
     */
    @Override
    public List<Flotilla> getFlotillas(final FlotillaType flotillaType) {
        return flotillas.get(flotillaType);
    }


    /**
     * Deploy the squadrons.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException If the scenario data cannot be properly loaded.
     */
    @Override
    public void deployAssets(final Scenario scenario) throws ScenarioException {
        flotillaAI.deploy(scenario, this);
        squadronAI.deploy(scenario, this);
        minefieldAI.deploy(scenario, this);

    }

    /**
     * This gets all of the player's squadrons for all nations.
     *
     * @return All of the player's squadrons.
     */
    @Override
    public List<Squadron> getSquadrons() {
        return squadrons
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    /**
     * Set the player's squadrons. This is only called on existing games.
     * The airfield contains the squadrons. So get the squadrons from
     * the airfield.
     *
     * Once the squadrons are known, then the region requirements which
     * depend upon the number of squadrons can be set.
     */
    @Override
    public void setSquadrons() {
        squadrons.clear();  // Clear out any stale data if it exists.

        for (Nation nation: nations) {

            List<Squadron> nationsSquadrons = airfields
                    .stream()
                    .flatMap(airfield -> airfield.getSquadrons(nation).stream())
                    .collect(Collectors.toList());

            squadrons.put(nation, nationsSquadrons);

            List<Region> regions = airfields
                    .stream()
                    .map(airfield -> airfield.getRegion(nation))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            regions.forEach(region -> region.setRequirements(nationsSquadrons));
        }
    }

    /**
     * Get the player's task force given its name.
     *
     * @param name The task force name.
     * @return The task force corresponding to the given name.
     */
    @Override
    public TaskForce getTaskForce(final String name) {
        return taskForceMap.get(name);
    }

    /**
     * This gets the enemy player's task force view given its name.
     *
     * @param name The name of the enemy task force.
     * @return The enemy player's task force view corresponding to the given name.
     */
    @Override
    public TaskForceView getEnemyTaskForce(final String name) {
        return enemyTaskForceMap.get(name);
    }

    /**
     * This gets the player's squadrons for the given nation.
     *
     * @param nation A nation BRITISH, ITALIAN, etc...
     * @param locationType Where the squadron is located: LAND or SEA.
     * @return A list of squadrons for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation, final SquadronLocationType locationType) {
        return locationType == SquadronLocationType.LAND
                ? squadrons.get(nation)
                : getTaskForceSquadrons(nation);
    }

    /**
     * This gets the player's squadrons for the given location type.
     *
     * @param locationType Where the squadron is located; LAND or SEA.
     * @return A list of squadrons at the given location.
     */
    @Override
    public List<Squadron> getSquadrons(final SquadronLocationType locationType) {
        return locationType == SquadronLocationType.LAND
                ? getLandSquadrons()
                : getTaskForceSquadrons();
    }

    /**
     * This gets the player's airbases which includes task forces with aircraft carriers.
     *
     * @return The player's airbases including task forces.
     */
    @Override
    public List<Airbase> getAirbases() {
        Stream<Airbase> taskForceAirbases = taskForces.stream().flatMap(taskForce -> taskForce.getAirbases().stream());
        Stream<Airbase> airfieldBases = getAirfields().stream().map(airfield -> airfield);

        return Stream.concat(taskForceAirbases, airfieldBases).collect(Collectors.toList());
    }

    /**
     * Get the player's airfield given its name.
     *
     * @param name The name of the airfield.
     * @return The airfield corresponding to the given name.
     */
    @Override
    public Airfield getAirfield(final String name) {
        return airfieldMap.get(name);
    }

    /**
     * Get the player's airbase given its name.
     *
     * @param name The name of the airbase.
     * @return The airbase corresponding to the given name.
     */
    @Override
    public Airbase getAirbase(final String name) {
        return airbaseMap.get(name);
    }

    /**
     * This gets the enemy player's airfield view given its name.
     *
     * @param name The name of the enemy airfield.
     * @return The enemy player's airfield view corresponding to the given name.
     */
    @Override
    public AirfieldView getEnemyAirfield(final String name) {
        return enemyAirfieldMap.get(name);
    }

    /**
     * Load the player's squadrons.
     *
     * @param scenario The selected scenario.
     * @throws SquadronException if the squadrons cannot be loaded.
     */
    @Override
    public void loadSquadrons(final Scenario scenario) throws SquadronException {
        squadrons.clear(); // Clear out any stale data if it exists.

        for (Nation nation: nations) {
            if (nation.isSquadronsPresent()) {
                loadNationSquadrons(scenario, nation);
            }
        }
    }

    /**
     * Get a port given its name.
     *
     * @param name The name of the port.
     * @return The port corresponding to the given name.
     */
    @Override
    public Port getPort(final String name) {
        return portMap.get(name);
    }

    /**
     * Get the enemy player's port view given its name.
     *
     * @param name The name of the enemy port.
     * @return The enemy port view corresponding to the given name.
     */
    @Override
    public PortView getEnemyPort(final String name) {
        return enemyPortMap.get(name);
    }

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @return A list of target for the given mission type.
     */
    @Override
    public List<Target> getTargets(final SeaMissionType missionType) {
        return seaTargetMap
                .get(missionType)
                .get();
    }

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @return A list of targets for the given mission type.
     */
    @Override
    public List<Target> getTargets(final AirMissionType missionType, final Nation nation) {
        return airTargetMap
                .get(missionType)
                .apply(nation);
    }

    /**
     * Get the enemy task force targets.
     *
     * @return A list of enemy task force targets.
     */
    private List<Target> getEnemyTaskForceTargets() {
        return enemyTaskForces
                .stream()
                .filter(TaskForceView::isSpotted)
                .map(TaskForceView::getEnemyTaskForce)
                .map(targetDAO::getEnemyTaskForceTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy airfield targets.
     *
     * @return A list of enemy airfield targets.
     */
    private List<Target> getEnemyAirfieldTargets() {
        return gameMap
                .getAirfields(side.opposite())
                .stream()
                .map(targetDAO::getEnemyAirfieldTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy port targets.
     *
     * @return A list of enemy port targets.
     */
    private List<Target> getEnemyPortTargets() {
        return gameMap
                .getPorts(side.opposite())
                .stream()
                .map(targetDAO::getEnemyPortTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the friendly task force targets.
     *
     * @return A list of friendly task force targets.
     */
    private List<Target> getFriendlyTaskForceTargets() {
        return taskForces
                .stream()
                .map(targetDAO::getFriendlyTaskForceTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the friendly airbase targets for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of friendly airbase targets.
     */
    private List<Target> getFriendlyAirbaseTargets(final Nation nation) {
        return airbaseMap
                .values()
                .stream()
                .filter(airbase -> airbase.canUse(nation))
                .map(targetDAO::getFriendlyAirbaseTarget)
                .collect(Collectors.toList());
    }

    /**
     * Load the task forces.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException Indicates the task forces could not be loaded.
     */
    private void loadTaskForces(final Scenario scenario) throws ScenarioException {
        taskForces = taskForceDAO.load(scenario, side);

        taskForceMap = taskForces
                .stream()
                .collect(Collectors.toMap(TaskForce::getName, taskForce -> taskForce));

        taskForces
                .stream()
                .flatMap(taskForce -> taskForce.getAirbases().stream())
                .forEach(airbase -> airbaseMap.put(airbase.getName(), airbase));
    }

    /**
     * Load the flotillas. Submarines and MTB's.
     *
     * @param scenario The selected scenario.
     */
    private void loadFlotillas(final Scenario scenario) {
        flotillas.clear(); // Clear out any stale data.

        for (FlotillaType flotillaType : FlotillaType.values()) {
            flotillas.put(flotillaType, flotillaDAO.load(scenario, side, flotillaType));
        }
    }

    /**
     * Load the given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param nation The nation.
     * @throws SquadronException if the squadrons cannot be loaded.
     */
    private void loadNationSquadrons(final Scenario scenario, final Nation nation) throws SquadronException {
         squadrons.put(nation, aviationPlant.load(scenario, side, nation));
    }

    private List<Squadron> getLandSquadrons() {
        return squadrons
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Get squadrons for the given nation that are stationed within the player's
     * task forces.
     *
     * @param nation The nation: BRITAIN or ITALIAN, etc...
     * @return All the squadrons of the given nation that are stationed within
     * this players task forces.
     */
    private List<Squadron> getTaskForceSquadrons(final Nation nation) {
        return taskForces
                .stream()
                .flatMap(taskForce -> taskForce.getSquadrons().stream())
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    private List<Squadron> getTaskForceSquadrons() {
        return taskForces
                .stream()
                .flatMap(taskForce -> taskForce.getSquadrons().stream())
                .collect(Collectors.toList());
    }

    private Set<Nation> getTaskForceSquadronNations() {
        return getTaskForceSquadrons()
                .stream()
                .map(Squadron::getNation)
                .collect(Collectors.toSet());
    }
}
