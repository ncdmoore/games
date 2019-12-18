package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
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
import engima.waratsea.model.minefield.MinefieldDAO;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronAI;
import engima.waratsea.model.squadron.SquadronDAO;
import engima.waratsea.model.squadron.deployment.SquadronDeploymentType;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceDAO;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryDAO;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

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

    private final FlotillaAI flotillaAI;

    private VictoryConditions victoryConditions;

    @Getter
    @Setter
    private Side side;

    @Getter
    private Set<Nation> nations;

    @Getter
    private List<TaskForce> taskForces;

    @Getter
    private List<TaskForceView> taskForceViews;

    private final Map<FlotillaType, List<Flotilla>> flotillas = new HashMap<>();

    private final Map<Nation, List<Squadron>> squadrons = new HashMap<>();

    @Getter
    private List<Airfield> airfields;

    @Getter
    private Map<String, Airfield> airfieldMap = new HashMap<>();

    @Getter
    private List<AirfieldView> enemyAirfields;

    @Getter
    private Map<String, AirfieldView> enemyAirfieldMap = new HashMap<>();

    @Getter
    private List<Port> ports;

    @Getter
    private Map<String, Port> portMap = new HashMap<>();

    @Getter
    private List<PortView> enemyPorts;

    @Getter
    private Map<String, PortView> enemyPortMap = new HashMap<>();

    @Getter
    private List<Minefield> minefields;

    private final Map<SquadronDeploymentType, BiConsumer<Scenario, Player>> deploymentMap = new HashMap<>();

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param victoryDAO Loads the player's victory conditions.
     * @param taskForceDAO Loads task force data.
     * @param taskForceViewDAO Loads task force view data.
     * @param flotillaDAO Loads flotilla data.
     * @param airfieldDAO Loads airfield data.
     * @param airfieldViewDAO Loads enemy airfield view data.
     * @param portDAO Loads port data.
     * @param portViewDAO Loads enemy port view data.
     * @param minefieldDAO Loads minefield data.
     * @param aviationPlant Loads squadron data.
     * @param flotillaAI Flotilla AI. Human Flotillas are deployed by the AI.
     * @param squadronAI Deploys the squadrons for fixed deployment scenarios.
     */
    //CHECKSTYLE:OFF
    @Inject
    public HumanPlayer(final GameMap gameMap,
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
                       final FlotillaAI flotillaAI,
                       final SquadronAI squadronAI) {
        //CHECKSTYLE:ON

        this.gameMap = gameMap;
        this.victoryDAO = victoryDAO;
        this.taskForceDAO = taskForceDAO;
        this.taskForceViewDAO = taskForceViewDAO;
        this.flotillaDAO = flotillaDAO;
        this.airfieldDAO = airfieldDAO;
        this.airfieldViewDAO = airfieldViewDAO;
        this.portDAO = portDAO;
        this.portViewDAO = portViewDAO;
        this.minefieldDAO = minefieldDAO;
        this.aviationPlant = aviationPlant;

        this.flotillaAI = flotillaAI;

        deploymentMap.put(SquadronDeploymentType.COMPUTER, squadronAI::deploy);
        deploymentMap.put(SquadronDeploymentType.HUMAN,    squadronAI::manualDeployment);
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
        scenario.setObjectives(victoryConditions.getObjectives());
    }

    /**
     * This sets the player's task forces.
     * @param scenario The selected scenario.
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

        scenario.setMinefieldForHumanSide(!minefields.isEmpty());

        loadTaskForces(scenario);
        loadFlotillas(scenario);

        scenario.setFlotillasForHumanSide(areFlotillasPresent());
    }

    /**
     * This builds the player's list of known targets.
     *
     * This must be called after buildAssets.
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

        taskForceViews = taskForceViewDAO.load(opposingPlayer.getTaskForces());
    }

    /**
     * Deploy assets. This is only called for new games.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException Indicates the assets could not be loaded.
     */
    @Override
    public void deployAssets(final Scenario scenario) throws ScenarioException {
        flotillaAI.deploy(scenario, this);

        // The scenario determines whether the human player deploys squadrons or the AI does.
        deploymentMap.get(scenario.getSquadron()).accept(scenario, this);
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
     * Set the player's squadrons. This is only called on existing games.
     * The airfield contains the squadrons. So get the squadrons from
     * the airfield.
     *
     * Once the squadrons are know, then the region requirements which
     * depend upon the number of squadrons can be set.
     */
    @Override
    public void setSquadrons() {
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

            regions.
                    forEach(region -> region.setRequirements(nationsSquadrons));
        }
    }

    /**
     * This gets all of the player's squadrons for all nations.
     *
     * @return All of the player's squadrons.
     */
    public List<Squadron> getSquadrons() {
        return squadrons
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
    }

    /**
     * This gets the player's squadrons for the given nation.
     *
     * @param nation A nation BRITISH, ITALIAN, etc...
     * @return A list of squadrons for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation) {
        return squadrons.get(nation);
    }

    /**
     * Determine the nations for this scenario and side.
     *
     * @param scenario The selected scenario.
     */
    public void loadSquadrons(final Scenario scenario) {
        for (Nation nation: nations) {
            loadNationSquadrons(scenario, nation);
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
    }

    /**
     * Load the flotillas.
     *
     * @param scenario The selected scenario.
     */
    private void loadFlotillas(final Scenario scenario)  {
        for (FlotillaType flotillaType : FlotillaType.values()) {
            flotillas.put(flotillaType, flotillaDAO.load(scenario, side, flotillaType));
        }
    }

    /**
     * Determine if any flotillas are present.
     *
     * @return True if any flotillas are present. False otherwise.
     */
    private boolean areFlotillasPresent() {
        return flotillas
                .entrySet()
                .stream()
                .anyMatch(entry -> !entry.getValue().isEmpty());   // Is the flotilla list empty
    }

    /**
     * Load the given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param nation The nation.
     */
    private void loadNationSquadrons(final Scenario scenario, final Nation nation) {
        squadrons.put(nation, aviationPlant.load(scenario, side, nation));
    }
}
