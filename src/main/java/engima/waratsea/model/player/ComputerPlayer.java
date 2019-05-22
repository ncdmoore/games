package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.MinefieldAI;
import engima.waratsea.model.minefield.MinefieldDAO;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronAI;
import engima.waratsea.model.squadron.SquadronDAO;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceDAO;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the computer player in the game.
 */
public class ComputerPlayer implements Player {

    private GameMap gameMap;
    private TaskForceDAO taskForceDAO;
    private AirfieldDAO airfieldDAO;
    private PortDAO portDAO;
    private SquadronDAO aviationPlant;
    private MinefieldDAO minefieldDAO;

    private SquadronAI squadronAI;
    private MinefieldAI minefieldAI;

    @Getter
    @Setter
    private Side side;

    @Getter
    private Set<Nation> nations;

    @Getter
    private List<TaskForce> taskForces;

    private Map<Nation, List<Squadron>> squadrons = new HashMap<>();

    @Getter
    private List<Airfield> airfields;

    @Getter
    private List<Port> ports;

    @Getter
    private List<Minefield> minefields;

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game map.
     * @param taskForceDAO Loads scenario data.
     * @param airfieldDAO Loads airfield data.
     * @param portDAO Loads port data.
     * @param minefieldDAO Loads minefield data.
     * @param aviationPlant Loads squadron data.
     * @param squadronAI squadron AI.
     * @param minefieldAI minefield AI.
     */
    //CHECKSTYLE:OFF
    @Inject
    public ComputerPlayer(final GameMap gameMap,
                          final TaskForceDAO taskForceDAO,
                          final AirfieldDAO airfieldDAO,
                          final PortDAO portDAO,
                          final MinefieldDAO minefieldDAO,
                          final SquadronDAO aviationPlant,
                          final SquadronAI squadronAI,
                          final MinefieldAI minefieldAI) {
        //CHECKSTYLE:ON
        this.gameMap = gameMap;
        this.taskForceDAO = taskForceDAO;
        this.airfieldDAO = airfieldDAO;
        this.portDAO = portDAO;
        this.aviationPlant = aviationPlant;
        this.minefieldDAO = minefieldDAO;
        this.squadronAI = squadronAI;
        this.minefieldAI = minefieldAI;
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

        loadSquadrons(scenario);
        loadTaskForces(scenario);

    }

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        taskForceDAO.save(scenario, side, taskForces);
        portDAO.save(scenario, side, ports);
        airfieldDAO.save(scenario, side, airfields);
        minefieldDAO.save(scenario, side, minefields);

        nations.forEach(nation -> aviationPlant.save(scenario, side, nation, squadrons.get(nation)));
    }

    /**
     * Deploy the squadrons.
     *
     * @param scenario The selected scenario.
     */
    public void deployAssets(final Scenario scenario) {
        squadronAI.deploy(scenario, this);
        minefieldAI.deploy(scenario, this);

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
     * Load the given nation's squadrons.
     *
     * @param scenario The selected scenario.
     * @param nation The nation.
     * @throws ScenarioException if the squadrons cannot be loaded.
     */
    private void loadNationSquadrons(final Scenario scenario, final Nation nation) throws ScenarioException {
         squadrons.put(nation, aviationPlant.load(scenario, side, nation));
    }
}
