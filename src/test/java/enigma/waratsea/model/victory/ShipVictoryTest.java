package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.Shipyard;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.taskForce.TaskForceState;
import engima.waratsea.model.taskForce.data.TaskForceData;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.VictoryConditionsData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShipVictoryTest {

    private static Ship battleShip;
    private static TaskForce taskForce;
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

        victoryConditionsFactory = injector.getInstance(VictoryConditionsFactory.class);

        Shipyard shipyard = injector.getInstance(Shipyard.class);

        String shipName = "BB08 Royal Sovereign";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);
        battleShip = shipyard.load(shipId);

        TaskForceFactory taskForceFactory = injector.getInstance(TaskForceFactory.class);

        TaskForceData data = new TaskForceData();
        data.setLocation("Tobruk");
        data.setState(TaskForceState.ACTIVE);
        data.setShips(new ArrayList<>(Arrays.asList("BB11 Nelson", "BB12 Rodney", "BB08 Royal Sovereign", "CL47 Dido", "CA12 York")));

        taskForce = taskForceFactory.create(Side.ALLIES, data);

        battleShip.setTaskForce(taskForce);
    }

    @Test
    public void testShipEvent() {

        int victoryPoints = 3;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("DAMAGED_PRIMARY");
        shipMatchData.setShipType("BATTLESHIP, AIRCRAFT_CARRIER");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);


        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.AXIS);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.DAMAGED_PRIMARY);
        event.setShip(battleShip);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

    }

    @Test
    public void testShipBombardEvent() {

        int victoryPoints = 5;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("BOMBARDMENT");
        shipMatchData.setSide(Side.ALLIES);
        shipMatchData.setShipType("BATTLESHIP");
        shipMatchData.setLocation("ANY_ENEMY_BASE");

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Tobruk");

        Assert.assertTrue(taskForce.atEnemyBase());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.BOMBARDMENT);
        event.setShip(taskForce.getShip("BB11 Nelson"));

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testShipSunkEvent() {

        int victoryPoints = 12;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("SUNK");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.AXIS);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.SUNK);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testShipSunkVictoryOverridePointsEvent() {

        int victoryPoints = 44;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("SUNK");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.AXIS);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.SUNK);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testShipUnloadEvent() {

        int victoryPoints = 12;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("CARGO_UNLOADED");
        shipMatchData.setShipType("CRUISER");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.CARGO_UNLOADED);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testShipUnloadVictoryOverridePointsEvent() {

        int victoryPoints = 4;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("CARGO_UNLOADED");
        shipMatchData.setShipType("CRUISER");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setLocation("Malta");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Malta");

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.CARGO_UNLOADED);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());
    }

    @Test
    public void testShipRequiredVictory() {
        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("CARGO_UNLOADED");
        shipMatchData.setShipType("CRUISER");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setLocation("Malta");
        shipMatchData.setSide(Side.ALLIES);

        List<ShipMatchData> matchers = new ArrayList<>();
        matchers.add(shipMatchData);

        ShipVictoryData shipRequiredVictoryData = new ShipVictoryData();
        shipRequiredVictoryData.setEvents(matchers);

        List<ShipVictoryData> required = new ArrayList<>();
        required.add(shipRequiredVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setRequiredShip(required);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertFalse(victory.requirementsMet());

        taskForce.setReference("Malta");

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.CARGO_UNLOADED);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertTrue(victory.requirementsMet());
    }

    @Test
    public void testShipRequiredVictoryPointsAchieved() {
        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("CARGO_UNLOADED");
        shipMatchData.setShipType("CRUISER");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setLocation("Malta");
        shipMatchData.setSide(Side.ALLIES);

        int requiredPoints = 10;

        List<ShipMatchData> matchers = new ArrayList<>();
        matchers.add(shipMatchData);

        ShipVictoryData shipRequiredVictoryData = new ShipVictoryData();
        shipRequiredVictoryData.setEvent(shipMatchData);
        shipRequiredVictoryData.setEvents(matchers);
        shipRequiredVictoryData.setRequiredPoints(requiredPoints);

        List<ShipVictoryData> required = new ArrayList<>();
        required.add(shipRequiredVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(required);
        victoryData.setRequiredShip(required);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertFalse(victory.requirementsMet());

        taskForce.setReference("Malta");

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.CARGO_UNLOADED);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertTrue(victory.requirementsMet());
    }

    @Test
    public void testShipRequiredVictoryPointsAchievedMultipleEvents() {
        int victoryPoints = 4;
        int requiredPoints = 8;

        taskForce.setReference("Malta");

        ShipMatchData shipMatchData1 = new ShipMatchData();
        shipMatchData1.setAction("CARGO_UNLOADED");
        shipMatchData1.setShipType("CRUISER");
        shipMatchData1.setName("CL47 Dido");
        shipMatchData1.setLocation("Malta");
        shipMatchData1.setSide(Side.ALLIES);

        ShipMatchData shipMatchData2 = new ShipMatchData();
        shipMatchData2.setAction("CARGO_UNLOADED");
        shipMatchData2.setShipType("CRUISER");
        shipMatchData2.setName("CA12 York");
        shipMatchData2.setLocation("Malta");
        shipMatchData2.setSide(Side.ALLIES);

        ShipMatchData shipMatchDataAll = new ShipMatchData();
        shipMatchDataAll.setAction("CARGO_UNLOADED");
        shipMatchDataAll.setShipType("CRUISER");
        shipMatchDataAll.setLocation("Malta");
        shipMatchDataAll.setSide(Side.ALLIES);

        List<ShipMatchData> matchers = new ArrayList<>();
        matchers.add(shipMatchDataAll);

        ShipVictoryData shipVictoryData1 = new ShipVictoryData();
        shipVictoryData1.setEvent(shipMatchData1);
        shipVictoryData1.setPoints(victoryPoints);

        ShipVictoryData shipVictoryData2 = new ShipVictoryData();
        shipVictoryData2.setEvent(shipMatchData2);
        shipVictoryData2.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData1);
        shipData.add(shipVictoryData2);

        ShipVictoryData shipRequiredVictoryData = new ShipVictoryData();
        shipRequiredVictoryData.setEvents(matchers);
        shipRequiredVictoryData.setRequiredPoints(requiredPoints);

        List<ShipVictoryData> requiredShipData = new ArrayList<>();
        requiredShipData.add(shipRequiredVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);
        victoryData.setRequiredShip(requiredShipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        // No ships have unloaded total victory should be 0.
        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Malta");

        ShipEvent event1 = new ShipEvent();
        event1.setAction(ShipEventAction.CARGO_UNLOADED);
        event1.setShip(taskForce.getShip("CL47 Dido"));

        event1.fire();

        // One ship has unloaded.
        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

        ShipEvent event2 = new ShipEvent();
        event2.setAction(ShipEventAction.CARGO_UNLOADED);
        event2.setShip(taskForce.getShip("CA12 York"));

        event2.fire();

        // Two ships have now unloaded.
        Assert.assertEquals(victoryPoints * 2, victory.getTotalVictoryPoints());

        Assert.assertTrue(victory.requirementsMet());

    }

    @Test
    public void testShipRequiredVictoryPointsNotAchievedMultipleEvents() {
        int victoryPoints = 4;
        int requiredPoints = 8;

        taskForce.setReference("Malta");

        ShipMatchData shipMatchData1 = new ShipMatchData();
        shipMatchData1.setAction("CARGO_UNLOADED");
        shipMatchData1.setShipType("CRUISER");
        shipMatchData1.setName("CL47 Dido");
        shipMatchData1.setLocation("Malta");
        shipMatchData1.setSide(Side.ALLIES);

        ShipMatchData shipMatchData2 = new ShipMatchData();
        shipMatchData2.setAction("CARGO_UNLOADED");
        shipMatchData2.setShipType("CRUISER");
        shipMatchData2.setName("CA12 York");
        shipMatchData2.setLocation("Alexandria");
        shipMatchData2.setSide(Side.ALLIES);

        ShipMatchData shipMatchDataAll = new ShipMatchData();
        shipMatchDataAll.setAction("CARGO_UNLOADED");
        shipMatchDataAll.setShipType("CRUISER");
        shipMatchDataAll.setLocation("Malta");
        shipMatchDataAll.setSide(Side.ALLIES);

        List<ShipMatchData> matchers = new ArrayList<>();
        matchers.add(shipMatchDataAll);

        ShipVictoryData shipVictoryData1 = new ShipVictoryData();
        shipVictoryData1.setEvent(shipMatchData1);
        shipVictoryData1.setPoints(victoryPoints);

        ShipVictoryData shipVictoryData2 = new ShipVictoryData();
        shipVictoryData2.setEvent(shipMatchData2);
        shipVictoryData2.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData1);
        shipData.add(shipVictoryData2);

        ShipVictoryData shipRequiredVictoryData = new ShipVictoryData();
        shipRequiredVictoryData.setEvents(matchers);
        shipRequiredVictoryData.setRequiredPoints(requiredPoints);

        List<ShipVictoryData> requiredShipData = new ArrayList<>();
        requiredShipData.add(shipRequiredVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);
        victoryData.setRequiredShip(requiredShipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        //No ships have unloaded. Victory should be 0.
        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Malta");

        ShipEvent event1 = new ShipEvent();
        event1.setAction(ShipEventAction.CARGO_UNLOADED);
        event1.setShip(taskForce.getShip("CL47 Dido"));

        event1.fire();

        // One ship has unloaded. Total victory should equal the victory points awarded for a
        // single ship unloading.
        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

        // Move the task force to a place where unloading should not result in any victory.
        // that satifies the overal required victory.
        taskForce.setReference("Alexandria");

        ShipEvent event2 = new ShipEvent();
        event2.setAction(ShipEventAction.CARGO_UNLOADED);
        event2.setShip(taskForce.getShip("CA12 York"));

        event2.fire();

        // Two ships have unloaded..
        Assert.assertEquals(victoryPoints * 2, victory.getTotalVictoryPoints());

        // The required victory conditions of 8 total points from unloading at Malta have not been met.
        // 4 victory points awarded at Malta. 4 victory points awarded at Alexandria.
        // The points awarded at Alexandria do not help met the Malta requirement.
        Assert.assertFalse(victory.requirementsMet());
    }

    @Test
    public void testShipRequiredVictoryPointsNotAchieved() {
        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("CARGO_UNLOADED");
        shipMatchData.setShipType("CRUISER");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setLocation("Malta");
        shipMatchData.setSide(Side.ALLIES);

        int requiredPoints = 300;

        List<ShipMatchData> matchers = new ArrayList<>();
        matchers.add(shipMatchData);

        ShipVictoryData shipRequiredVictoryData = new ShipVictoryData();
        shipRequiredVictoryData.setEvent(shipMatchData);
        shipRequiredVictoryData.setEvents(matchers);
        shipRequiredVictoryData.setRequiredPoints(requiredPoints);

        List<ShipVictoryData> required = new ArrayList<>();
        required.add(shipRequiredVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(required);
        victoryData.setRequiredShip(required);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertFalse(victory.requirementsMet());

        taskForce.setReference("Malta");

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.CARGO_UNLOADED);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        ShipEvent nonMatchedEvent = new ShipEvent();
        nonMatchedEvent.setAction(ShipEventAction.CARGO_UNLOADED);
        nonMatchedEvent.setShip(taskForce.getShip("CA12 York"));

        nonMatchedEvent.fire();

        Assert.assertFalse(victory.requirementsMet());
    }

    /**
     * This test a scenario specific event that overrides the default victory condition.
     */
    @Test
    public void testShipScenarioOverride() {
        int victoryPoints = 4;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("BOMBARDMENT");
        shipMatchData.setName("CL47 Dido");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        int scenarioVictoryPoints = 5;

        ShipMatchData ScenarioShipMatchData = new ShipMatchData();
        ScenarioShipMatchData.setAction("BOMBARDMENT");
        ScenarioShipMatchData.setName("CL47 Dido");
        ScenarioShipMatchData.setSide(Side.ALLIES);

        ShipVictoryData scenarioShipVictoryData = new ShipVictoryData();
        scenarioShipVictoryData.setEvent(ScenarioShipMatchData);
        scenarioShipVictoryData.setPoints(scenarioVictoryPoints);

        List<ShipVictoryData> scenarioShipData = new ArrayList<>();
        scenarioShipData.add(scenarioShipVictoryData);

        victoryData.setScenarioShip(scenarioShipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Tobruk");

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.BOMBARDMENT);
        event.setShip(taskForce.getShip("CL47 Dido"));

        event.fire();

        Assert.assertEquals(scenarioVictoryPoints, victory.getTotalVictoryPoints());
    }

    /**
     * This test a victory condition that requires multiple occerences for any
     * victory points to be rewarded. A single occurence of the underlying event
     * results in no victory points awarded.
     */
    @Test
    public void testShipBombardmentMultipleOccurrencesRequired() {
        int victoryPoints = 5;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("BOMBARDMENT");
        shipMatchData.setSide(Side.ALLIES);
        shipMatchData.setShipType("BATTLESHIP, CRUISER");
        shipMatchData.setLocation("ANY_ENEMY_BASE");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);
        shipVictoryData.setRequiredOccurrences(2);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Tobruk");

        Assert.assertTrue(taskForce.atEnemyBase());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.BOMBARDMENT);
        event.setShip(taskForce.getShip("BB11 Nelson"));

        event.fire();

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

        event.setShip(taskForce.getShip("CA12 York"));
        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

        event.setShip(taskForce.getShip("BB08 Royal Sovereign"));
        event.fire();

        Assert.assertEquals(victoryPoints * 2, victory.getTotalVictoryPoints());
    }

    /**
     * This test a multiple occurrence required condition that overrides a default condition.
     */
    @Test
    public void testShipBombardmentMultipleOccurrencesRequiredOverride() {
        //Build a default victory condition.
        int victoryPoints = 5;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("BOMBARDMENT");
        shipMatchData.setSide(Side.ALLIES);
        shipMatchData.setShipType("BATTLESHIP, CRUISER");
        shipMatchData.setLocation("ANY_ENEMY_BASE");
        shipMatchData.setSide(Side.ALLIES);

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);

        VictoryConditionsData victoryData = new VictoryConditionsData();
        victoryData.setDefaultShip(shipData);


        //Build a scenario specific victory condition.
        int scenarioVictoryPoints = 4;

        ShipMatchData scenarioShipMatchData = new ShipMatchData();
        scenarioShipMatchData.setAction("BOMBARDMENT");
        scenarioShipMatchData.setSide(Side.ALLIES);
        scenarioShipMatchData.setShipType("BATTLESHIP, CRUISER");
        scenarioShipMatchData.setLocation("Tobruk");

        ShipVictoryData scenarioShipVictoryData = new ShipVictoryData();
        scenarioShipVictoryData.setEvent(scenarioShipMatchData);
        scenarioShipVictoryData.setPoints(scenarioVictoryPoints);
        scenarioShipVictoryData.setRequiredOccurrences(2);

        List<ShipVictoryData> scenarioShipData = new ArrayList<>();
        scenarioShipData.add(scenarioShipVictoryData);

        victoryData.setScenarioShip(scenarioShipData);

        VictoryConditions victory = victoryConditionsFactory.create(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        taskForce.setReference("Tobruk");

        Assert.assertTrue(taskForce.atEnemyBase());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.BOMBARDMENT);
        event.setShip(taskForce.getShip("BB11 Nelson"));

        event.fire();

        Assert.assertEquals(0, victory.getTotalVictoryPoints());   // No points are awarded for the first event.

        event.fire();

        Assert.assertEquals(scenarioVictoryPoints, victory.getTotalVictoryPoints()); // Points are awarded on the second event.

        event.setShip(taskForce.getShip("CA12 York"));
        event.fire();

        Assert.assertEquals(scenarioVictoryPoints, victory.getTotalVictoryPoints()); // No additional points awarded on the third event.

        event.setShip(taskForce.getShip("BB08 Royal Sovereign"));
        event.fire();

        Assert.assertEquals(scenarioVictoryPoints * 2, victory.getTotalVictoryPoints()); // Points awarded on the 4th event.
    }
}
