package enigma.waratsea.model.game;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.AppProps;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.ships.TaskForce;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
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

        gameTitle.setValue("bombAlley");

        injector.getInstance(AppProps.class);                                                                           // Load the main application properties.

        game = injector.getInstance(Game.class);
    }

    @Test
    public void testTaskForces() throws ScenarioException {
        List<Scenario> scenarios = game.initScenarios();
        game.setScenario(scenarios.get(0));
        game.setHumanSide(Side.ALLIES);
        game.start();

        List<TaskForce> humanForces = game.getHumanPlayer().getTaskForces();
        List<TaskForce> computerForces = game.getComputerPlayer().getTaskForces();

        String humanShipName = humanForces.get(0).getShips().get(0);
        String computerShipName = computerForces.get(0).getShips().get(0);

        log.info("human ship {}", humanShipName);
        log.info("computer ship {},", computerShipName);

        assert (!humanShipName.equals(computerShipName));
    }
}
