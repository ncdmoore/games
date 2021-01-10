package enigma.waratsea.model.game.rules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.TurnIndex;
import engima.waratsea.model.game.rules.AirOperationRules;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class AirOperationRulesTest {
    private AirOperationRules rules;
    private Weather weather;
    private Turn turn;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        rules = injector.getInstance(AirOperationRules.class);
        weather = injector.getInstance(Weather.class);
        turn = injector.getInstance(Turn.class);
    }

    @Test
    public void testDayClear() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);

        weather.setCurrent(WeatherType.CLEAR);   // Make sure weather is clear as turn.start may have changed it.

        int prob = rules.getProbabilityCrash(AirbaseType.LAND, SquadronAction.TAKE_OFF);

        Assert.assertEquals(0, prob);
    }

    @Test
    public void testDaySquall() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.SQUALL);

        turn.start(scenario);

        weather.setCurrent(WeatherType.SQUALL); // Make sure weather is squall as turn.start may have changed it.

        int prob = rules.getProbabilityCrash(AirbaseType.LAND, SquadronAction.TAKE_OFF);

        Assert.assertEquals(16, prob);

        prob = rules.getProbabilityCrash(AirbaseType.SEAPLANE, SquadronAction.TAKE_OFF);

        Assert.assertEquals(33, prob);

    }

    @Test
    public void testDayStorm() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.STORM);

        turn.start(scenario);

        weather.setCurrent(WeatherType.STORM); // Make sure weather is gale as turn.start may have changed it.

        int prob = rules.getProbabilityCrash(AirbaseType.LAND, SquadronAction.TAKE_OFF);

        Assert.assertEquals(100, prob);

        prob = rules.getProbabilityCrash(AirbaseType.SEAPLANE, SquadronAction.TAKE_OFF);

        Assert.assertEquals(100, prob);
    }

    @Test
    public void testDayStormLand() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.STORM);

        turn.start(scenario);

        weather.setCurrent(WeatherType.STORM); // Make sure weather is gale as turn.start may have changed it.

        int prob = rules.getProbabilityCrash(AirbaseType.LAND, SquadronAction.LAND);

        Assert.assertEquals(33, prob);

        prob = rules.getProbabilityCrash(AirbaseType.SEAPLANE, SquadronAction.LAND);

        Assert.assertEquals(50, prob);
    }

    @Test
    public void testDayGale() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.GALE);

        turn.start(scenario);

        weather.setCurrent(WeatherType.GALE); // Make sure weather is gale as turn.start may have changed it.

        int prob = rules.getProbabilityCrash(AirbaseType.LAND, SquadronAction.TAKE_OFF);

        Assert.assertEquals(100, prob);
    }
}
