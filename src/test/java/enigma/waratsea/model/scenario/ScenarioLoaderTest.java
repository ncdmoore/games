package enigma.waratsea.model.scenario;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioLoader;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceLoader;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class ScenarioLoaderTest {

    private static GameTitle gameTitle;
    private static Config config;
    private static Injector injector;


    private List<String> games = new ArrayList<>(Arrays.asList("bombAlley", "coralSea"));


    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);
        config = injector.getInstance(Config.class);

    }

    @Test
    public void testScenarioSummaryLoading()  {
        games.forEach(this::scenarioSummaryLoading);
    }

    //todo consider all scenarios.
    @Test
    public void testTaskForceLoading() {
        taskForceLoading("bombAlley", "firstSortie");
        taskForceLoading("bombAlley", "capeTeulada");
        //taskForceLoading("coralSea", "coralSea");
    }

    private void scenarioSummaryLoading(String gameName) {

        try {
            gameTitle.setValue(gameName);
            ScenarioLoader loader = injector.getInstance(ScenarioLoader.class);

            List<Scenario> scenarios = loader.load();

            assert (!scenarios.isEmpty());                                                                              // Ensure that at least one scenario was loaded.

            EnumSet<WeatherType> allWeather = EnumSet.allOf(WeatherType.class);

            assert (allWeather.contains(scenarios.get(0).getWeather()));                                                // Ensure that the weather can be de-serialized correctly.
        } catch (Exception ex) {
            assert (false);
        }


    }

    private void taskForceLoading(String gameName, String scenarioName)  {

        try {
            Scenario scenario = new Scenario();
            scenario.setName(scenarioName);
            scenario.setTitle(scenarioName);

            gameTitle.setValue(gameName);
            config.setScenario(scenario);

            TaskForceLoader loader = injector.getInstance(TaskForceLoader.class);

            List<TaskForce> taskForces = loader.load(scenario, Side.ALLIES);

            assert (!taskForces.isEmpty());

            taskForces = loader.load(scenario, Side.AXIS);

            assert (!taskForces.isEmpty());
        } catch (Exception ex) {
            assert (false);
        }
    }
}
