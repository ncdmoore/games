package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldLoader;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.nation.Nation;
import engima.waratsea.model.game.nation.NationProps;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.base.port.PortLoader;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLoader;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceLoader;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

    private NationProps nationProps;
    private GameMap gameMap;
    private TaskForceLoader taskForceLoader;
    private AirfieldLoader airfieldBuilder;
    private PortLoader portBuilder;
    private SquadronLoader aviationPlant;

    @Getter
    @Setter
    private Side side;

    @Getter
    private List<Nation> nations;

    @Getter
    private List<TaskForce> taskForces;

    @Getter
    private Map<Nation, List<Squadron>> squadrons = new HashMap<>();

    @Getter
    private List<Airfield> airfields;

    @Getter
    private List<Port> ports;

    /**
     * Constructor called by guice.
     *
     * @param nationProps The nation properties.
     * @param gameMap The game map.
     * @param taskForceLoader Loads scenario data.
     * @param airfieldBuilder Loads airfield data.
     * @param portBuilder Loads port data.
     * @param aviationPlant Loads squadron data.
     */
    @Inject
    public HumanPlayer(final NationProps nationProps,
                       final GameMap gameMap,
                       final TaskForceLoader taskForceLoader,
                       final AirfieldLoader airfieldBuilder,
                       final PortLoader portBuilder,
                       final SquadronLoader aviationPlant) {
        this.nationProps = nationProps;
        this.gameMap = gameMap;
        this.taskForceLoader = taskForceLoader;
        this.airfieldBuilder = airfieldBuilder;
        this.portBuilder = portBuilder;
        this.aviationPlant = aviationPlant;
    }

    /**
     * This sets the player's task forces.
     * @param scenario The selected scenario.
     */
    @Override
    public void buildAssets(final Scenario scenario) throws ScenarioException {
        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = gameMap.getAirfields(side);
        ports = gameMap.gerPorts(side);

        taskForces = taskForceLoader.load(scenario, side);

        loadSquadrons(scenario);
    }

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        taskForceLoader.save(scenario, side, taskForces);
        portBuilder.save(scenario, side, ports);
        airfieldBuilder.save(scenario, side, airfields);
    }

    /**
     * Determine the nations for this scenario and side.
     *
     * @param scenario The selected scenario.
     */
    private void loadSquadrons(final Scenario scenario) {
        nations = nationProps.getNations(scenario, side);
        nations.forEach(nation -> loadNationSquadrons(scenario, nation));
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
