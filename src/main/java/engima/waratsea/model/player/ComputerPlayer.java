package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.airfield.AirfieldLoader;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.port.Port;
import engima.waratsea.model.port.PortLoader;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceLoader;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is the computer player in the game.
 */
public class ComputerPlayer implements Player {

    private TaskForceLoader taskForceLoader;
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
     * @param taskForceLoader Loads scenario data.
     * @param airfieldBuilder Loads airfield data.
     * @param portBuilder Loads port data.
     */
    @Inject
    public ComputerPlayer(final TaskForceLoader taskForceLoader,
                          final AirfieldLoader airfieldBuilder,
                          final PortLoader portBuilder) {
        this.taskForceLoader = taskForceLoader;
        this.airfieldBuilder = airfieldBuilder;
        this.portBuilder = portBuilder;
    }

    /**
     * This sets the player's task forces.
     * @param scenario The selected scenario.
     */
    @Override
    public void buildAssets(final Scenario scenario) throws ScenarioException {
        taskForces = taskForceLoader.load(scenario, side);

        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = airfieldBuilder.load(side);
        ports = portBuilder.load(side);
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
}
