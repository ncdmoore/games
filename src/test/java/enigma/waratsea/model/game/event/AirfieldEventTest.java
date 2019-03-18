package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventAction;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcherFactory;
import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AirfieldEventTest {

    private static AirfieldFactory airfieldFactory;
    private static AirfieldEventMatcherFactory matcherFactory;

    @BeforeClass
    public static void setup() throws Exception {

        Injector injector = Guice.createInjector(new TestModule());

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);

        gameMap.load(scenario);

        airfieldFactory = injector.getInstance(AirfieldFactory.class);
        matcherFactory = injector.getInstance(AirfieldEventMatcherFactory.class);
    }

    @Test
    public void testAirfieldDamagedEvent() {

        AirfieldData airfieldData = new AirfieldData();
        airfieldData.setName("Rome");

        Airfield airfield = airfieldFactory.create(Side.ALLIES, airfieldData);

        AirfieldEvent event = new AirfieldEvent();
        event.setAirfield(airfield);
        event.setAction(AirfieldEventAction.DAMAGE);

        AirfieldMatchData matchData = new AirfieldMatchData();
        matchData.setAction(AirfieldEventAction.DAMAGE);
        matchData.setSide(Side.ALLIES);
        matchData.setName("Rome");

        AirfieldEventMatcher matcher = matcherFactory.create(matchData);


        Assert.assertTrue(matcher.match(event));
    }

}
