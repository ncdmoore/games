package engima.waratsea.model.player;

import com.google.inject.Inject;
import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.airfield.AirfieldLoader;
import engima.waratsea.model.port.Port;
import engima.waratsea.model.port.PortLoader;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceLoader;
import engima.waratsea.utility.PersistentUtility;
import lombok.Getter;
import lombok.Setter;
import engima.waratsea.model.game.Side;

import java.util.List;

/**
 * This is the human player in the game.
 */
public class HumanPlayer implements Player {

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
     * @param airfieldLoader Loads airfield data.
     * @param portBuilder Loads port data.
     */
    @Inject
    public HumanPlayer(final TaskForceLoader taskForceLoader,
                       final AirfieldLoader airfieldLoader,
                       final PortLoader portBuilder) {
        this.taskForceLoader = taskForceLoader;
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
        taskForces = taskForceLoader.load(scenario, side);

        //Note the airfields and ports used depend upon the map which is set by the scenario.
        airfields = airfieldBuilder.build(side);
        ports = portBuilder.build(side);
    }

    /**
     * This saves the player's assets.
     *
     * @param scenario The selected scenario.
     */
    @Override
    public void saveAssets(final Scenario scenario) {
        taskForceLoader.save(scenario, side, PersistentUtility.getData(taskForces));
    }

}
