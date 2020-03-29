package enigma.waratsea.model.game.event;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.squadron.SquadronEventAction;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcher;
import engima.waratsea.model.game.event.squadron.SquadronEventMatcherFactory;
import engima.waratsea.model.game.event.squadron.data.SquadronMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.data.SquadronData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class SquadronEventTest {
    private static SquadronEventMatcherFactory matcherFactory;
    private static SquadronFactory squadronFactory;
    private static AirfieldFactory airfieldFactory;
    private static GameMap gameMap;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        gameMap = injector.getInstance(GameMap.class);
        gameMap.load(scenario);

        matcherFactory = injector.getInstance(SquadronEventMatcherFactory.class);
        squadronFactory = injector.getInstance(SquadronFactory.class);
        airfieldFactory = injector.getInstance(AirfieldFactory.class);

    }

    @Test
    public void testSquadronDestroyedEvent() {
        SquadronData squadronData = new SquadronData();
        squadronData.setModel("BF109E");
        squadronData.setStrength(SquadronStrength.FULL);

        Squadron squadron = squadronFactory.create(Side.AXIS, Nation.GERMAN, squadronData);

        SquadronMatchData matchData = new SquadronMatchData();
        matchData.setSide(Side.AXIS);
        matchData.setAircraftModel("BF109E");
        matchData.setAction("DESTROYED");


        SquadronEventMatcher matcher = matcherFactory.create(matchData);

        SquadronEvent event = new SquadronEvent();
        event.setSquadron(squadron);
        event.setAction(SquadronEventAction.DESTROYED);

        matcher.log();

        Assert.assertTrue(matcher.match(event));
    }

    @Test
    public void testSquadronFerryEvent() {
        AirfieldData airfieldData = new AirfieldData();
        airfieldData.setName("Malta");
        airfieldData.setSide(Side.ALLIES);
        airfieldData.setLocation(gameMap.convertNameToReference("Malta"));
        airfieldData.setLandingType(new ArrayList<>(Arrays.asList(LandingType.LAND, LandingType.SEAPLANE)));


        Airfield airfield = airfieldFactory.create(airfieldData);

        SquadronData squadronData = new SquadronData();
        squadronData.setModel("Blenheim");
        squadronData.setStrength(SquadronStrength.FULL);


        Squadron squadron = squadronFactory.create(Side.ALLIES, Nation.BRITISH, squadronData);
        squadron.setAirbase(airfield);
        squadron.setReference("Alexandria");

        SquadronMatchData matchData = new SquadronMatchData();
        matchData.setSide(Side.ALLIES);
        matchData.setAircraftModel("Blenheim");
        matchData.setAction("ARRIVAL");
        matchData.setLocation("Alexandria");
        matchData.setStartingLocation("Malta");


        SquadronEventMatcher matcher = matcherFactory.create(matchData);

        SquadronEvent event = new SquadronEvent();
        event.setSquadron(squadron);
        event.setAction(SquadronEventAction.ARRIVAL);

        matcher.log();

        Assert.assertTrue(matcher.match(event));
    }
}
