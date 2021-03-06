package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.GameName;
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
import java.util.List;

public class AirfieldVictoryTest {
    private static List<Airfield> alliedAirfields;
    private static List<Airfield> axisAirfields;
    private static VictoryConditionsFactory victoryConditionsFactory;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);
        gameMap.load(scenario);

        alliedAirfields = gameMap.getAirfields(Side.ALLIES);
        axisAirfields = gameMap.getAirfields(Side.AXIS);

        victoryConditionsFactory = injector.getInstance(VictoryConditionsFactory.class);
    }

    @Test
    public void testAirfieldDamage() {
        int victoryPoints = 3;

        AirfieldMatchData airfieldMatchData = new AirfieldMatchData();
        airfieldMatchData.setAction("DAMAGE");
        airfieldMatchData.setSide(Side.AXIS);
        airfieldMatchData.setName(axisAirfields.get(1).getName());

        AirfieldVictoryData airfieldVictoryData = new AirfieldVictoryData();
        airfieldVictoryData.setEvent(airfieldMatchData);
        airfieldVictoryData.setPoints(victoryPoints);

        List<AirfieldVictoryData> airfieldData = new ArrayList<>();
        airfieldData.add(airfieldVictoryData);


        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioAirfield(airfieldData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        AirfieldEvent event = new AirfieldEvent();
        event.setAction(AirfieldEventAction.DAMAGE);
        event.setAirfield(axisAirfields.get(1));
        event.setValue(1);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testAirfieldRepair() {
        int victoryPoints = -3;

        AirfieldMatchData airfieldMatchData = new AirfieldMatchData();
        airfieldMatchData.setAction("REPAIR");
        airfieldMatchData.setSide(Side.ALLIES);
        airfieldMatchData.setName(alliedAirfields.get(0).getName());

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
        event.setAirfield(alliedAirfields.get(0));
        event.setValue(1);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }
}
