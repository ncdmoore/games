package enigma.waratsea.model.game;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.TurnIndex;
import engima.waratsea.model.game.TurnType;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class TurnTest {
    private GameTitle gameTitle;
    private Turn turn;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);  //The game instance must be injected first!

        turn = injector.getInstance(Turn.class);
    }

    @Test
    public void winterBombAlleyTurnTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.TWILIGHT);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);
        Assert.assertSame(TurnType.NIGHT, turn.getTrue(Calendar.DECEMBER));
    }

    @Test
    public void summerBombAlleyTurnTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.TWILIGHT);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);
        Assert.assertSame(TurnType.DAY, turn.getTrue(Calendar.JUNE));
    }
    @Test
    public void winterCoralSeaTurnTest() {
        gameTitle.setName(GameName.CORAL_SEA);

        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.TWILIGHT);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);
        Assert.assertSame(TurnType.NIGHT, turn.getTrue(Calendar.DECEMBER));
    }

    @Test
    public void summerCoralSeaTurnTest() {
        gameTitle.setName(GameName.CORAL_SEA);

        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.TWILIGHT);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);
        Assert.assertSame(TurnType.NIGHT, turn.getTrue(Calendar.JUNE));
    }

    @Test
    public void turnTypeTest() {

        Date date = new Date();

        Scenario scenario = new Scenario();
        scenario.setDate(date);
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        Assert.assertSame(TurnType.DAY, turn.getType());

        turn.next();   //2

        Assert.assertSame(TurnType.DAY, turn.getType());

        turn.next();   //3

        Assert.assertSame(TurnType.DAY, turn.getType());

        turn.next();   //4

        Assert.assertSame(TurnType.TWILIGHT, turn.getType());

        turn.next();   // 5

        Assert.assertSame(TurnType.NIGHT, turn.getType());

        turn.next();  // 6

        Assert.assertSame(TurnType.NIGHT, turn.getType());

        turn.next();   // 7  Next day.

        Assert.assertSame(TurnType.DAY, turn.getType());

        Date newDate = turn.getDate();

        calendar.setTime(newDate);
        int newDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        Assert.assertEquals(dayOfMonth, newDayOfMonth);
    }
}
