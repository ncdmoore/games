package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.airfield.AirfieldLoader;
import engima.waratsea.model.port.Port;
import engima.waratsea.model.port.PortLoader;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.scenario.ScenarioLoader;
import engima.waratsea.model.taskForce.TaskForce;
import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

import java.util.List;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

    private ScenarioLoader scenarioLoader;
    private AirfieldLoader airfieldBuilder;
    private PortLoader portBuilder;

    @Getter
    @Setter
    private Side side;

    @Getter
    private List<TaskForce> taskForces;

    @Getter
    private List<Airfield> airfields;

    @Getter
    private List<Port> ports;

    /**
     * Constructor called by guice.
     * @param scenarioLoader Loads scenario data.
     * @param airfieldLoader Loads airfield data.
     * @param portBuilder Loads port data.
     */
    @Inject
    public HumanPlayer(final ScenarioLoader scenarioLoader,
                       final AirfieldLoader airfieldLoader,
                       final PortLoader portBuilder) {
        this.scenarioLoader = scenarioLoader;
        this.airfieldBuilder = airfieldLoader;
        this.portBuilder = portBuilder;
    }

    /**
     * This sets the player's task forces.
     *
     * @param scenario The selected scenario.
     * @throws ScenarioException Indicates the task forces could not be loaded.
     */
    @Override
    public void buildAssets(final Scenario scenario) throws ScenarioException {
        taskForces = scenarioLoader.loadTaskForce(scenario, side);

        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = airfieldBuilder.build(side);
        ports = portBuilder.build(side);
    }

}
