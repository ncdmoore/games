package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldLoader;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventAction;
import engima.waratsea.model.game.event.airfield.data.AirfieldMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;
import engima.waratsea.model.victory.data.AirfieldVictoryData;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AirfieldVictoryTest {
    private static List<Airfield> airfields;
    private static VictoryConditionsFactory victoryConditionsFactory;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);
        gameMap.load(scenario);

        victoryConditionsFactory = injector.getInstance(VictoryConditionsFactory.class);

        AirfieldLoader airfieldLoader = injector.getInstance(AirfieldLoader.class);

        List<String> airfieldNames = new ArrayList<>(Collections.singletonList("Malta"));
        airfields = airfieldLoader.load(Side.ALLIES, airfieldNames);
    }

    @Test
    public void testAirfieldDamage() {
        int victoryPoints = 3;

        AirfieldMatchData airfieldMatchData = new AirfieldMatchData();
        airfieldMatchData.setAction("DAMAGE");
        airfieldMatchData.setSide(Side.ALLIES);

        AirfieldVictoryData airfieldVictoryData = new AirfieldVictoryData();
        airfieldVictoryData.setEvent(airfieldMatchData);
        airfieldVictoryData.setPoints(victoryPoints);

        List<AirfieldVictoryData> airfieldData = new ArrayList<>();
        airfieldData.add(airfieldVictoryData);


        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioAirfield(airfieldData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.AXIS);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        AirfieldEvent event = new AirfieldEvent();
        event.setAction(AirfieldEventAction.DAMAGE);
        event.setAirfield(airfields.get(0));
        event.setData(1);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testAirfieldRepair() {
        int victoryPoints = -3;

        AirfieldMatchData airfieldMatchData = new AirfieldMatchData();
        airfieldMatchData.setAction("REPAIR");
        airfieldMatchData.setSide(Side.ALLIES);

        AirfieldVictoryData airfieldVictoryData = new AirfieldVictoryData();
        airfieldVictoryData.setEvent(airfieldMatchData);
        airfieldVictoryData.setPoints(victoryPoints);

        List<AirfieldVictoryData> airfieldData = new ArrayList<>();
        airfieldData.add(airfieldVictoryData);


        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioAirfield(airfieldData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.AXIS);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        AirfieldEvent event = new AirfieldEvent();
        event.setAction(AirfieldEventAction.REPAIR);
        event.setAirfield(airfields.get(0));
        event.setData(1);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }
}
