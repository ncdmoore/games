package enigma.waratsea.model.scenario;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioDAO;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceDAO;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

@Slf4j
public class ScenarioLoaderTest {

    private static GameTitle gameTitle;
    private static Resource config;
    private static GameMap gameMap;
    private static Injector injector;


    private List<GameName> games = new ArrayList<>(Arrays.asList(GameName.BOMB_ALLEY, GameName.CORAL_SEA));


    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);
        config = injector.getInstance(Resource.class);

        gameMap = injector.getInstance(GameMap.class);
    }

    @Test
    public void testScenarioSummaryLoading()  {
        games.forEach(this::scenarioSummaryLoading);
    }

    //todo consider all scenarios.
    @Test
    public void testTaskForceLoading() {
        taskForceLoading(GameName.BOMB_ALLEY, "firstSortie", "june1940");
        taskForceLoading(GameName.BOMB_ALLEY, "capeTeulada", "june1940");
        //taskForceLoading("coralSea", "coralSea");
    }

    private void scenarioSummaryLoading(GameName gameName) {

        try {
            gameTitle.setName(gameName);
            ScenarioDAO scenarioDAO = injector.getInstance(ScenarioDAO.class);

            List<Scenario> scenarios = scenarioDAO.load();

            assert (!scenarios.isEmpty());                                                                              // Ensure that at least one scenario was loaded.

            EnumSet<WeatherType> allWeather = EnumSet.allOf(WeatherType.class);

            assert (allWeather.contains(scenarios.get(0).getWeather()));                                                // Ensure that the weather can be de-serialized correctly.
        } catch (Exception ex) {
            assert (false);
        }


    }

    private void taskForceLoading(GameName gameName, String scenarioName, String mapName)  {

        try {
            gameTitle.setName(gameName);

            Scenario scenario = new Scenario();
            scenario.setName(scenarioName);
            scenario.setTitle(scenarioName);
            scenario.setMap(mapName);

            config.setScenario(scenario.getName());

            gameMap.load(scenario);

            TaskForceDAO taskForceDAO = injector.getInstance(TaskForceDAO.class);

            List<TaskForce> taskForces = taskForceDAO.load(scenario, Side.ALLIES);

            assert (!taskForces.isEmpty());

            taskForces = taskForceDAO.load(scenario, Side.AXIS);

            assert (!taskForces.isEmpty());
        } catch (Exception ex) {
            log.error("Task force loading failed", ex);
            assert (false);
        }
    }
}
