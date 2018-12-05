package enigma.waratsea.model.scenario;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioLoader;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;
import engima.waratsea.model.AppProps;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.model.weather.WeatherType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class ScenarioLoaderTest {

    private static GameTitle gameTitle;
    private static AppProps props;
    private static ScenarioLoader scenarioLoader;


    private List<String> games = new ArrayList<>(Arrays.asList("bombAlley", "coralSea"));


    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);                                                              //The game instance must be injected first!

        props = injector.getInstance(AppProps.class);                                                                   // Load the main application properties.

        scenarioLoader = injector.getInstance(ScenarioLoader.class);
    }

    @Test
    public void testScenarioSummaryLoading()  {
        games.forEach(this::scenarioSummaryLoading);

    }

    //todo consider all scenarios.
    @Test
    public void testTaskForceLoading() {
        taskForceLoading("bombAlley", "firstSortie");
        taskForceLoading("coralSea", "coralSea");
    }

    private void scenarioSummaryLoading(String gameName) {

        try {
            gameTitle.setValue(gameName);
            props.init(gameTitle.getValue());

            List<Scenario> scenarios = scenarioLoader.loadSummaries();

            assert (!scenarios.isEmpty());                                                                              // Ensure that at least one scenario was loaded.

            EnumSet<WeatherType> allWeather = EnumSet.allOf(WeatherType.class);

            assert (allWeather.contains(scenarios.get(0).getWeather()));                                                // Ensure that the weather can be de-serialized correctly.
        } catch (Exception ex) {
            assert (false);
        }


    }

    private void taskForceLoading(String gameName, String scenarioName)  {

        try {
            gameTitle.setValue(gameName);

            List<TaskForce> taskForces = scenarioLoader.loadTaskForce(scenarioName, Side.ALLIES);

            assert (!taskForces.isEmpty());

            taskForces = scenarioLoader.loadTaskForce(scenarioName, Side.AXIS);

            assert (!taskForces.isEmpty());
        } catch (Exception ex) {
            assert (false);
        }
    }
}
