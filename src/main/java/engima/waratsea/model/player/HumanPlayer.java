package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.flotilla.FlotillaAI;
import engima.waratsea.model.flotilla.FlotillaDAO;
import engima.waratsea.model.flotilla.FlotillaType;
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
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryDAO;
import engima.waratsea.model.victory.VictoryException;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final FlotillaDAO flotillaDAO;
    private final AirfieldDAO airfieldDAO;
    private final PortDAO portDAO;
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

    private final Map<FlotillaType, List<Flotilla>> flotillas = new HashMap<>();

    private final Map<Nation, List<Squadron>> squadrons = new HashMap<>();

    @Getter
    private List<Airfield> airfields;

    @Getter
    private List<Port> ports;

    @Getter
    private List<Minefield> minefields;

    private final Map<SquadronDeploymentType, BiConsumer<Scenario, Player>> deploymentMap = new HashMap<>();


    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param victoryDAO Loads the player's victory conditions.
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
                       final VictoryDAO victoryDAO,
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
        this.victoryDAO = victoryDAO;
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
        nations = gameMap.getNations(side);
        airfields = gameMap.getAirfields(side);
        ports = gameMap.getPorts(side);
        minefields = gameMap.getMinefields(side);

        scenario.setMinefieldForHumanSide(!minefields.isEmpty());

        loadSquadrons(scenario);
        loadTaskForces(scenario);
        loadFlotillas(scenario);

        scenario.setFlotillasForHumanSide(areFlotillasPresent());
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

        nations.forEach(nation -> aviationPlant.save(scenario, side, nation, squadrons.get(nation)));

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
     * @throws ScenarioException Indicates the squadrons could not be loaded.
     */
    private void loadNationSquadrons(final Scenario scenario, final Nation nation) throws ScenarioException {
        squadrons.put(nation, aviationPlant.load(scenario, side, nation));
    }
}
