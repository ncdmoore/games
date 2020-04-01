package engima.waratsea.model.player;

import com.google.inject.Inject;
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
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryDAO;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    private VictoryConditions victoryConditions;

    @Getter @Setter private Side side;
    @Getter private Set<Nation> nations;
    @Getter private List<TaskForce> taskForces;
    @Getter private Map<String, TaskForce> taskForceMap;
    @Getter private List<TaskForceView> enemyTaskForces;
    @Getter private Map<String, TaskForceView> enemyTaskForceMap;
    @Getter private List<Airfield> airfields;
    @Getter private Map<String, Airfield> airfieldMap;
    @Getter private List<AirfieldView> enemyAirfields;
    @Getter private Map<String, AirfieldView> enemyAirfieldMap;
    @Getter private List<Port> ports;
    @Getter private Map<String, Port> portMap;
    @Getter private List<PortView> enemyPorts;
    @Getter private Map<String, PortView> enemyPortMap;
    @Getter private List<Minefield> minefields;

    private final Map<FlotillaType, List<Flotilla>> flotillas = new HashMap<>();
    private final Map<Nation, List<Squadron>> squadrons = new HashMap<>();

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
        Stream.of(FlotillaType.values()).forEach(flotillaType -> flotillaDAO.save(scenario, side, flotillas.get(flotillaType)));
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
     * Load the player's squadrons.
     *
     * @param scenario The selected scenario.
     * @throws SquadronException if the squadrons cannot be loaded.
     */
    @Override
    public void loadSquadrons(final Scenario scenario) throws SquadronException {
        squadrons.clear(); // Clear out any stale data if it exists.

        for (Nation nation: nations) {
            loadNationSquadrons(scenario, nation);
        }
    }
    /**
     * Get the friendly airfield targets for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of friendly airfield targets.
     */
    @Override
    public List<Target> getFriendlyAirfieldTargets(final Nation nation) {
        return gameMap
                .getNationAirfields(side, nation)
                .stream()
                .map(targetDAO::getFriendlyAirfieldTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy airfield targets.
     *
     * @return A list of enemy airfield targets.
     */
    @Override
    public List<Target> getEnemyAirfieldTargets() {
        return gameMap
                .getAirfields(side.opposite())
                .stream()
                .map(targetDAO::getEnemyAirfieldTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy task force targets.
     *
     * @return A list of enemy task force targets.
     */
    @Override
    public List<Target> getEnemyTaskForceTargets() {
        return enemyTaskForces
                .stream()
                .filter(TaskForceView::isSpotted)
                .map(TaskForceView::getEnemyTaskForce)
                .map(targetDAO::getEnemyTaskForceTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get the enemy port targets.
     *
     * @return A list of enemy port targets.
     */
    @Override
    public List<Target> getEnemyPortTargets() {
        return gameMap
                .getPorts(side.opposite())
                .stream()
                .map(targetDAO::getEnemyPortTarget)
                .collect(Collectors.toList());
    }

    /**
     * Get a list of targets for the given mission type.
     *
     * @param missionType The type of mission.
     * @return A list of targets for the given mission type.
     */
    @Override
    public List<Target> getTargets(final AirMissionType missionType, final Nation nation) {
        switch (missionType) {
            case FERRY:
                return getFriendlyAirfieldTargets(nation);
            case LAND_STRIKE:
            case SWEEP_AIRFIELD:
                return getEnemyAirfieldTargets();
            case NAVAL_PORT_STRIKE:
            case SWEEP_PORT:
                return getEnemyPortTargets();
            case NAVAL_TASK_FORCE_STRIKE:
                return getEnemyTaskForceTargets();
            default:
                log.error("Unknown mission type: '{}'", missionType);
                return Collections.emptyList();
        }
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
    }

    /**
     * Load the flotillas.
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

    /**
     * Get squadrons for the given nation that are stationed within the player's
     * task forces.
     *
     * @param nation The nation: BRITIAN or ITALIAN, etc...
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
}
