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
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AirfieldEventTest {

    private static AirfieldFactory factory;

    @BeforeClass
    public static void setup() throws Exception {

        Injector injector = Guice.createInjector(new TestModule());

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);

        gameMap.load(scenario);

        factory = injector.getInstance(AirfieldFactory.class);
    }

        @Test
    public void testAirfieldDamagedEvent() {

        AirfieldData data = new AirfieldData();
        data.setName("Rome");
        data.setLocation("Rome");

        Airfield airfield = factory.create(Side.ALLIES, data);

        AirfieldEvent event = new AirfieldEvent();
        event.setAirfield(airfield);
        event.setAction(AirfieldEventAction.DAMAGE);

        AirfieldEventMatcher matcher = new AirfieldEventMatcher();
        matcher.setSide(Side.ALLIES);
        matcher.setAction(AirfieldEventAction.DAMAGE);
        matcher.setName("Rome");

        Assert.assertTrue(matcher.match(event));
    }

}
