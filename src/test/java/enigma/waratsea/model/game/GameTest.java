package enigma.waratsea.model.game;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.AppProps;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.victory.VictoryException;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class GameTest {

    private Game game;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);                                                    //The game instance must be injected first!

        gameTitle.setName(GameName.BOMB_ALLEY);

        injector.getInstance(AppProps.class);                                                                           // Load the main application properties.

        game = injector.getInstance(Game.class);
    }

    @Test
    public void testTaskForces() throws ScenarioException, MapException, VictoryException {
        List<Scenario> scenarios = game.initScenarios();
        game.setScenario(scenarios.get(0));
        game.setHumanSide(Side.ALLIES);
        game.startNew();

        List<TaskForce> humanForces = game.getHumanPlayer().getTaskForces();
        List<TaskForce> computerForces = game.getComputerPlayer().getTaskForces();

        String humanShipName = humanForces.get(0).getShips().get(0).getName();
        String computerShipName = computerForces.get(0).getShips().get(0).getName();

        log.info("human ship {}", humanShipName);
        log.info("computer ship {}", computerShipName);

        assert (!humanShipName.equals(computerShipName));

        List<Airfield> humanAirfields = game.getHumanPlayer().getAirfields();
        List<Airfield> computerAirfields = game.getComputerPlayer().getAirfields();

        String humanAirfieldName = humanAirfields.get(0).getName();
        String computerAirfieldName = computerAirfields.get(0).getName();

        log.info("human airfield {}", humanAirfieldName);
        log.info("computer airfield {}", computerAirfieldName);

        Assert.assertNotEquals(humanAirfieldName, computerAirfieldName);


        List<Port> humanPorts = game.getHumanPlayer().getPorts();
        List<Port> computerPorts = game.getComputerPlayer().getPorts();

        String humanPortName = humanPorts.get(0).getName();
        String computerPortName = computerPorts.get(0).getName();

        log.info("human port {}", humanPortName);
        log.info("computer port {}", computerPortName);

        Assert.assertNotEquals(humanPortName, computerPortName);
    }
}
