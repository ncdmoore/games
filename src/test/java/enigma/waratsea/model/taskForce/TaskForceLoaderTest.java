package enigma.waratsea.model.taskForce;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.GameType;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceDAO;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class TaskForceLoaderTest {
    private static TaskForceDAO taskForceDAO;
    private static Config config;
    private static GameMap gameMap;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        config = injector.getInstance(Config.class);
        gameMap = injector.getInstance(GameMap.class);
        taskForceDAO = injector.getInstance(TaskForceDAO.class);

        gameTitle.setName(GameName.BOMB_ALLEY);
    }

    @Test
    public void testSavedTaskForce() throws Exception {
        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The First Sortie");
        scenario.setMap("june1940");

        config.setScenario(scenario.getName());

        gameMap.load(scenario);

        List<TaskForce> alliedTaskForces = taskForceDAO.load(scenario, Side.ALLIES);

        taskForceDAO.save(scenario, Side.ALLIES, alliedTaskForces);

        config.setType(GameType.EXISTING);

        alliedTaskForces = taskForceDAO.load(scenario, Side.ALLIES);

        Assert.assertNotNull(alliedTaskForces);

        Ship ship = alliedTaskForces.get(0).getShips().get(0);

        Assert.assertNotNull(ship);
    }
}
