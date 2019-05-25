package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaAI;
import engima.waratsea.model.flotilla.FlotillaDAO;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
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
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

    private GameMap gameMap;
    private TaskForceDAO taskForceDAO;
    private FlotillaDAO flotillaDAO;
    private AirfieldDAO airfieldDAO;
    private PortDAO portDAO;
    private SquadronDAO aviationPlant;
    private MinefieldDAO minefieldDAO;

    private FlotillaAI flotillaAI;

    @Getter
    @Setter
    private Side side;

    @Getter
    private Set<Nation> nations;

    @Getter
    private List<TaskForce> taskForces;

    @Getter
    private List<Flotilla> flotillas;

    private Map<Nation, List<Squadron>> squadrons = new HashMap<>();

    @Getter
    private List<Airfield> airfields;

    @Getter
    private List<Port> ports;

    @Getter
    private List<Minefield> minefields;

    private Map<SquadronDeploymentType, BiConsumer<Scenario, Player>> deploymentMap = new HashMap<>();


    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param taskForceDAO Loads task force data.
     * @param flotillaDAO Loads flotilla data.
     * @param airfieldDAO Loads airfield data.
     * @param portDAO Loads port data.
     * @param minefieldDAO Loads minefield data.
     * @param aviationPlant Loads squadron data.
     * @param flotillaAI Flotilla AI. Human Flotillas are deployed by the AI.
     * @param squadronAI Deploys the squadrons for fixed deployment scenarios.
     */
    //CHECKSTYLE:OFF
    @Inject
    public HumanPlayer(final GameMap gameMap,
                       final TaskForceDAO taskForceDAO,
                       final FlotillaDAO flotillaDAO,
                       final AirfieldDAO airfieldDAO,
                       final PortDAO portDAO,
                       final MinefieldDAO minefieldDAO,
                       final SquadronDAO aviationPlant,
                       final FlotillaAI flotillaAI,
                       final SquadronAI squadronAI) {
        //CHECKSTYLE:ON
        this.gameMap = gameMap;
        this.taskForceDAO = taskForceDAO;
        this.flotillaDAO = flotillaDAO;
        this.airfieldDAO = airfieldDAO;
        this.portDAO = portDAO;
        this.minefieldDAO = minefieldDAO;
        this.aviationPlant = aviationPlant;

        this.flotillaAI = flotillaAI;

        deploymentMap.put(SquadronDeploymentType.COMPUTER, squadronAI::deploy);
        deploymentMap.put(SquadronDeploymentType.HUMAN,    squadronAI::manualDeployment);
    }

    /**
     * This sets the player's task forces.
     * @param scenario The selected scenario.
     */
    @Override
    public void buildAssets(final Scenario scenario) throws ScenarioException {
        //Note the airfields and ports used depend upon the map which is set by the scenario.
        nations = gameMap.getNations(side);
        airfields = gameMap.getAirfields(side);
        ports = gameMap.getPorts(side);
        minefields = gameMap.getMinefields(side);

        scenario.setMinefieldForHumanSide(!minefields.isEmpty());

        loadSquadrons(scenario);
        loadTaskForces(scenario);
        loadFlotillas(scenario);
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
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        taskForceDAO.save(scenario, side, taskForces);
        flotillaDAO.save(scenario, side, flotillas);
        portDAO.save(scenario, side, ports);
        airfieldDAO.save(scenario, side, airfields);
        minefieldDAO.save(scenario, side, minefields);

        nations.forEach(nation -> aviationPlant.save(scenario, side, nation, squadrons.get(nation)));

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
     * @throws ScenarioException Indicates the squadrons could not be loaded.
     */
    private void loadSquadrons(final Scenario scenario) throws ScenarioException {
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
        flotillas = flotillaDAO.load(scenario, side);
    }

    /**
     * Load the given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param nation The nation.
     * @throws ScenarioException Indicates the squadrons could not be loaded.
     */
    private void loadNationSquadrons(final Scenario scenario, final Nation nation) throws ScenarioException {
        squadrons.put(nation, aviationPlant.load(scenario, side, nation));
    }
}
