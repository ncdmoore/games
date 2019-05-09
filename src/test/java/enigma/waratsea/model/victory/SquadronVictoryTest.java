package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.squadron.SquadronEventAction;
import engima.waratsea.model.game.event.squadron.data.SquadronMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.Shipyard;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;
import engima.waratsea.model.victory.data.SquadronVictoryData;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SquadronVictoryTest {
    private static Squadron alliedSquadron;
    private static Squadron axisSquadron;
    private static VictoryConditionsFactory victoryConditionsFactory;
    private static Shipyard shipyard;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        shipyard = injector.getInstance(Shipyard.class);

        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The first Sortie");
        scenario.setMap("june1940");

        GameMap gameMap = injector.getInstance(GameMap.class);
        gameMap.load(scenario);

        victoryConditionsFactory = injector.getInstance(VictoryConditionsFactory.class);

        SquadronFactory squadronFactory = injector.getInstance(SquadronFactory.class);

        SquadronData data = new SquadronData();
        data.setModel("Hurricane-1");

        alliedSquadron = squadronFactory.create(Side.ALLIES, data);

        data.setModel("BF109F");

        axisSquadron = squadronFactory.create(Side.AXIS, data);
    }

    @Test
    public void testSquadronArrival() {
        int victoryPoints = 3;

        SquadronMatchData squadronMatchData = new SquadronMatchData();
        squadronMatchData.setAction("ARRIVAL");
        squadronMatchData.setSide(Side.ALLIES);
        squadronMatchData.setAircraftModel("Hurricane-1");

        SquadronVictoryData squadronVictoryData = new SquadronVictoryData();
        squadronVictoryData.setEvent(squadronMatchData);
        squadronVictoryData.setPoints(victoryPoints);

        List<SquadronVictoryData> squadronData = new ArrayList<>();
        squadronData.add(squadronVictoryData);


        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioSquadron(squadronData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        SquadronEvent event = new SquadronEvent();
        event.setAction(SquadronEventAction.ARRIVAL);
        event.setSquadron(alliedSquadron);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }


    @Test
    public void testSquadronDamage() {
        int victoryPoints = 6;

        SquadronMatchData squadronMatchData = new SquadronMatchData();
        squadronMatchData.setAction("DAMAGED");
        squadronMatchData.setSide(Side.AXIS);
        squadronMatchData.setAircraftModel("BF109F");

        SquadronVictoryData squadronVictoryData = new SquadronVictoryData();
        squadronVictoryData.setEvent(squadronMatchData);
        squadronVictoryData.setPoints(victoryPoints);

        List<SquadronVictoryData> squadronData = new ArrayList<>();
        squadronData.add(squadronVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioSquadron(squadronData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        SquadronEvent event = new SquadronEvent();
        event.setAction(SquadronEventAction.DAMAGED);
        event.setSquadron(axisSquadron);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testSquadronScenarioOverride() {
        int victoryPoints = 4;

        SquadronMatchData squadronMatchData = new SquadronMatchData();
        squadronMatchData.setAction("DESTROYED");
        squadronMatchData.setSide(Side.ALLIES);

        SquadronVictoryData squadronVictoryData = new SquadronVictoryData();
        squadronVictoryData.setEvent(squadronMatchData);
        squadronVictoryData.setPoints(victoryPoints);

        List<SquadronVictoryData> squadronData = new ArrayList<>();
        squadronData.add(squadronVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultSquadron(squadronData);

        int scenarioVictoryPoints = 5;

        SquadronMatchData ScenarioSquadronMatchData = new SquadronMatchData();
        ScenarioSquadronMatchData.setAction("DESTROYED");
        ScenarioSquadronMatchData.setSide(Side.ALLIES);

        SquadronVictoryData scenarioSquadronVictoryData = new SquadronVictoryData();
        scenarioSquadronVictoryData.setEvent(ScenarioSquadronMatchData);
        scenarioSquadronVictoryData.setPoints(scenarioVictoryPoints);

        List<SquadronVictoryData> scenarioSquadronData = new ArrayList<>();
        scenarioSquadronData.add(scenarioSquadronVictoryData);

        victoryData.setScenarioSquadron(scenarioSquadronData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());


        SquadronEvent event = new SquadronEvent();
        event.setAction(SquadronEventAction.DESTROYED);
        event.setSquadron(alliedSquadron);

        event.fire();

        Assert.assertEquals(scenarioVictoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testSquadronArrivalFromCarrier() throws Exception {
        int victoryPoints = 10;

        SquadronMatchData squadronMatchData = new SquadronMatchData();
        squadronMatchData.setAction("ARRIVAL");
        squadronMatchData.setSide(Side.ALLIES);
        squadronMatchData.setAircraftModel("Hurricane-1");
        squadronMatchData.setStartingLocation("CVL01 Argus-1");
        squadronMatchData.setLocation("Malta");

        SquadronVictoryData squadronVictoryData = new SquadronVictoryData();
        squadronVictoryData.setEvent(squadronMatchData);
        squadronVictoryData.setPoints(victoryPoints);

        List<SquadronVictoryData> squadronData = new ArrayList<>();
        squadronData.add(squadronVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setScenarioSquadron(squadronData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        SquadronEvent event = new SquadronEvent();
        event.setAction(SquadronEventAction.ARRIVAL);
        event.setSquadron(alliedSquadron);

        Airbase airbase = buildAircraftCarrier("CVL01 Argus-1");
        alliedSquadron.setAirfield(airbase);
        alliedSquadron.setLocation("Malta");

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

        airbase = buildAircraftCarrier("CV06 Victorious-1");
        alliedSquadron.setAirfield(airbase);

        event.fire();

        // The above event should not match. Thus, the victory should not change.
        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

    }

    private Airbase buildAircraftCarrier(final String shipName) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);
        Ship ship = shipyard.load(shipId);
        return (Airbase) ship;
    }
}
