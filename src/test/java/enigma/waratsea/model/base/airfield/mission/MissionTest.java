package enigma.waratsea.model.base.airfield.mission;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionFactory;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.base.airfield.mission.state.AirMissionAction;
import engima.waratsea.model.base.airfield.mission.state.AirMissionState;
import engima.waratsea.model.enemy.views.airfield.AirfieldView;
import engima.waratsea.model.enemy.views.airfield.AirfieldViewFactory;
import engima.waratsea.model.enemy.views.airfield.data.AirfieldViewData;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.TurnIndex;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetFactory;
import engima.waratsea.model.target.TargetType;
import engima.waratsea.model.target.data.TargetData;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MissionTest {
    private static Turn turn;
    private static Game game;
    private static MissionFactory missionFactory;
    private static AirfieldFactory airfieldFactory;
    private static SquadronFactory squadronFactory;
    private static RegionFactory regionFactory;
    private static TargetFactory targetFactory;
    private static AirfieldViewFactory airfieldViewFactory;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        turn = injector.getInstance(Turn.class);
        game = injector.getInstance(Game.class);
        missionFactory = injector.getInstance(MissionFactory.class);
        airfieldFactory = injector.getInstance(AirfieldFactory.class);
        squadronFactory = injector.getInstance(SquadronFactory.class);
        regionFactory = injector.getInstance(RegionFactory.class);
        targetFactory = injector.getInstance(TargetFactory.class);
        airfieldViewFactory = injector.getInstance(AirfieldViewFactory.class);

        game.setHumanSide(Side.ALLIES);
        initTurn();
    }

    @Test
    public void testFerryEtaOneTurn() {
        Airfield endingAirfield = buildAboukirAirfield();
        Target friendlyAirfield = buildFriendlyAirfieldTarget(endingAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testFerry(1, squadron, endingAirfield, friendlyAirfield);
    }

    @Test
    public void testFerryEtaTwoTurns() {
        Airfield endingAirfield = buildFamagustaAirfield();
        Target friendlyAirfield = buildFriendlyAirfieldTarget(endingAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testFerry(2, squadron, endingAirfield, friendlyAirfield);
    }

    @Test
    public void testLandAttackEtaOneTurn() {
        Airfield enemyAirfield = buildDernaAirfield();
        Target enemyAirfieldTarget = buildEnemyAirfield(enemyAirfield);
        Squadron squadron = buildWellingtonSquadron();

        testLandAttack(1, squadron, enemyAirfield, enemyAirfieldTarget);
    }

    @Test
    public void testLandAttackEtaTwoTurns() {
        Airfield enemyAirfield = buildDernaAirfield();
        Target enemyAirfieldTarget = buildEnemyAirfield(enemyAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testLandAttack(2, squadron, enemyAirfield, enemyAirfieldTarget);
    }

    @Test
    public void testFerryRecall() {
        Airfield endingAirfield = buildFamagustaAirfield();
        Target friendlyAirfield = buildFriendlyAirfieldTarget(endingAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testFerryRecall(squadron, endingAirfield, friendlyAirfield);
    }

    @Test
    public void testLandAttackEtaTwoTurns2() {
        Airfield enemyAirfield = buildRhodesAirfield();
        Target enemyAirfieldTarget = buildEnemyAirfield(enemyAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testLandAttack(2, squadron, enemyAirfield, enemyAirfieldTarget);
    }

    @Test
    public void testLandAttackEtaTwoTurnsRecall() {
        Airfield enemyAirfield = buildRhodesAirfield();
        Target enemyAirfieldTarget = buildEnemyAirfield(enemyAirfield);
        Squadron squadron = buildSunderlandSquadron();

        testLandAttackRecall(squadron, enemyAirfield, enemyAirfieldTarget);
    }

    @Test
    public void testDistantCap() {
        Region region = buildEgyptRegion();
        Airfield airfield = buildAlexandriaAirfield();

        //create TaskForce
        //create TaskForceTarget
        Squadron squadron = buildHurricane();

        airfield.addRegion(region);
        airfield.addSquadron(squadron);

    }

    private void testFerry(final int targetEta, final Squadron squadron, final Airfield endingAirfield, final Target friendlyAirfield) {
        Region region = buildEgyptRegion();

        Airfield startingAirfield = buildAlexandriaAirfield();

        startingAirfield.addRegion(region);
        endingAirfield.addRegion(region);

        startingAirfield.addSquadron(squadron);

        Deencapsulation.setField(friendlyAirfield, "airbase", endingAirfield);

        MissionData data = new MissionData();
        data.setAirbase(startingAirfield);
        data.setNation(squadron.getNation());
        data.setTarget(friendlyAirfield.getName());
        data.setSquadronMap(getSquadronMap(List.of(squadron)));

        AirMission mission = missionFactory.createFerry(data);

        Deencapsulation.setField(mission, "endingAirbase", friendlyAirfield);
        mission.addSquadrons();

        Assert.assertEquals(AirMissionState.READY, mission.getState());

        mission.doAction(AirMissionAction.CREATE);

        Assert.assertEquals(AirMissionState.LAUNCHING, mission.getState());
        Assert.assertEquals(SquadronState.QUEUED_FOR_MISSION, squadron.getState()); // Squadron is queued.

        int turns = 1;
        mission.doAction(AirMissionAction.EXECUTE);                                 // Execute a turn.

        int distance = friendlyAirfield.getDistance(startingAirfield);
        int range = squadron.getRange(SquadronConfig.NONE);
        int eta = (distance / range) + (distance % range > 0 ? 1 : 0);

        int turnsToTarget = Deencapsulation.getField(mission, "turnsToTarget");

        Assert.assertEquals(targetEta, eta);
        Assert.assertEquals(eta, turnsToTarget);

        for (int i = turns; i < turnsToTarget; i++) {
            Assert.assertEquals(AirMissionState.OUT_BOUND, mission.getState());     // Mission takes multiple turns and is out-bound.
            Assert.assertEquals(SquadronState.ON_MISSION, squadron.getState());     // Squadron is on the mission.
            game.getTurn().next();                                                  // Start a new game turn.
            mission.doAction(AirMissionAction.EXECUTE);                             // Execute the new game turn.
            turns++;
        }

        Assert.assertEquals(AirMissionState.DONE, mission.getState());              // Mission is done.

        Assert.assertEquals(endingAirfield, squadron.getHome());                    // Squadron has a new home.
        Assert.assertEquals(SquadronState.HANGER, squadron.getState());             // Squadron is in the hanger.
    }

    private void testFerryRecall(final Squadron squadron, final Airfield endingAirfield, final Target friendlyAirfield) {
        Region region = buildEgyptRegion();

        Airfield startingAirfield = buildAlexandriaAirfield();

        startingAirfield.addRegion(region);
        endingAirfield.addRegion(region);

        startingAirfield.addSquadron(squadron);

        Deencapsulation.setField(friendlyAirfield, "airbase", endingAirfield);

        MissionData data = new MissionData();
        data.setAirbase(startingAirfield);
        data.setNation(squadron.getNation());
        data.setTarget(friendlyAirfield.getName());
        data.setSquadronMap(getSquadronMap(List.of(squadron)));

        AirMission mission = missionFactory.createFerry(data);

        Deencapsulation.setField(mission, "endingAirbase", friendlyAirfield);
        mission.addSquadrons();

        Assert.assertEquals(AirMissionState.READY, mission.getState());

        mission.doAction(AirMissionAction.CREATE);

        Assert.assertEquals(AirMissionState.LAUNCHING, mission.getState());
        Assert.assertEquals(SquadronState.QUEUED_FOR_MISSION, squadron.getState()); // Squadron is queued.

        mission.doAction(AirMissionAction.EXECUTE);                                 // Execute a turn.

        int distance = friendlyAirfield.getDistance(startingAirfield);
        int range = squadron.getRange(SquadronConfig.NONE);
        int eta = (distance / range) + (distance % range > 0 ? 1 : 0);

        int turnsToTarget = Deencapsulation.getField(mission, "turnsToTarget");

        final int targetEta = 2;
        Assert.assertEquals(targetEta, eta);
        Assert.assertEquals(eta, turnsToTarget);

        // Add the starting airfield to the player's list of airbases, so the ferry mission
        // can find it and create a target based off of it. This allows the squadrons to
        // land at the new target which is their original starting airfield.
        Map<String, Airbase> airbaseMap = Map.of(startingAirfield.getName(), startingAirfield);
        Player humanPlayer = game.getPlayer(Side.ALLIES);

        Deencapsulation.setField(humanPlayer, "airbaseMap", airbaseMap);

        mission.doAction(AirMissionAction.RECALL);

        // The recall changes the missions target to be the starting airfield.
        // Make sure this new target has its backing airbase.
        Target startingAirbase = mission.getTarget();
        Deencapsulation.setField(startingAirbase, "airbase", startingAirfield);

        Assert.assertEquals(AirMissionState.IN_BOUND, mission.getState());         // Mission is in-bound.

        turnsToTarget = Deencapsulation.getField(mission, "turnsToTarget");

        Assert.assertEquals(1, turnsToTarget);                            // Mission should be able to return in a single turn since
                                                                                   // only a single turn has been executed.

        mission.doAction(AirMissionAction.EXECUTE);                                // Execute the new game turn.

        Assert.assertEquals(AirMissionState.DONE, mission.getState());             // Mission is done.
        Assert.assertEquals(startingAirfield, squadron.getHome());                 // Squadron the same home.
        Assert.assertEquals(SquadronState.HANGER, squadron.getState());            // Squadron is in the hanger.
    }

    private void testLandAttack(final int targetEta, final Squadron squadron, final Airfield enemyAirfield, final Target enemyAirfieldTarget) {
        Region region = buildEgyptRegion();

        Airfield airfield = buildAlexandriaAirfield();

        airfield.addRegion(region);

        airfield.addSquadron(squadron);

        AirfieldView enemyAirfieldView = buildAirfieldView(enemyAirfield);

        Deencapsulation.setField(enemyAirfieldTarget, "airfieldView", enemyAirfieldView);

        MissionData data = new MissionData();
        data.setAirbase(airfield);
        data.setNation(squadron.getNation());
        data.setTarget(enemyAirfieldView.getName());
        data.setSquadronMap(getSquadronMap(List.of(squadron)));

        AirMission mission = missionFactory.createLandStrike(data);

        Deencapsulation.setField(mission, "targetAirbase", enemyAirfieldTarget);
        mission.addSquadrons();

        Assert.assertEquals(AirMissionState.READY, mission.getState());

        mission.doAction(AirMissionAction.CREATE);

        Assert.assertEquals(AirMissionState.LAUNCHING, mission.getState());
        Assert.assertEquals(SquadronState.QUEUED_FOR_MISSION, squadron.getState()); // Squadron is queued.

        int turns = 1;
        mission.doAction(AirMissionAction.EXECUTE);                                 // Execute a turn.

        int distance = enemyAirfieldTarget.getDistance(airfield);
        int range = squadron.getRange(SquadronConfig.NONE);
        int eta = (distance / range) + (distance % range > 0 ? 1 : 0);

        int turnsToTarget = Deencapsulation.getField(mission, "turnsToTarget");
        int turnsToHome = Deencapsulation.getField(mission, "turnsToHome");

        Assert.assertEquals(targetEta, eta);
        Assert.assertEquals(eta, turnsToTarget);

        for (int i = turns; i < turnsToTarget; i++) {
            Assert.assertEquals(AirMissionState.OUT_BOUND, mission.getState());     // Mission takes multiple turns and is out-bound.
            Assert.assertEquals(SquadronState.ON_MISSION, squadron.getState());     // Squadron is on the mission.
            game.getTurn().next();                                                  // Start a new game turn.
            turns++;
            mission.doAction(AirMissionAction.EXECUTE);                             // Execute the new game turn.
        }

        if (mission.getSquadrons().getDestroyedSquadronCount() == 0) {
            for (int i = turns; i < turnsToHome; i++) {
                Assert.assertEquals(AirMissionState.IN_BOUND, mission.getState());  // Mission takes multiple turns and is in-bound.
                Assert.assertEquals(SquadronState.ON_MISSION, squadron.getState()); // Squadron is on the mission.
                game.getTurn().next();                                              // Start a new game turn.
                turns++;
                mission.doAction(AirMissionAction.EXECUTE);                         // Execute the new game turn.
            }

            Assert.assertEquals(AirMissionState.DONE, mission.getState());          // Mission is done.


            Assert.assertEquals(airfield, squadron.getHome());                      // Squadron has a same home.
            Assert.assertEquals(SquadronState.HANGER, squadron.getState());         // Squadron is in the hanger.
        } else {
            Assert.assertEquals(0, airfield.getSquadrons().size());        // The only squadron was shot down.
            Assert.assertEquals(SquadronState.DESTROYED, squadron.getState());      // Squadron is destroyed.
            Assert.assertNull(squadron.getHome());                                  // The squadron is now homeless
        }
    }

    private void testLandAttackRecall(final Squadron squadron, final Airfield enemyAirfield, final Target enemyAirfieldTarget) {
        Region region = buildEgyptRegion();

        Airfield airfield = buildAlexandriaAirfield();

        airfield.addRegion(region);

        airfield.addSquadron(squadron);

        AirfieldView enemyAirfieldView = buildAirfieldView(enemyAirfield);

        Deencapsulation.setField(enemyAirfieldTarget, "airfieldView", enemyAirfieldView);

        MissionData data = new MissionData();
        data.setAirbase(airfield);
        data.setNation(squadron.getNation());
        data.setTarget(enemyAirfieldView.getName());
        data.setSquadronMap(getSquadronMap(List.of(squadron)));

        AirMission mission = missionFactory.createLandStrike(data);

        Deencapsulation.setField(mission, "targetAirbase", enemyAirfieldTarget);
        mission.addSquadrons();

        Assert.assertEquals(AirMissionState.READY, mission.getState());

        mission.doAction(AirMissionAction.CREATE);

        Assert.assertEquals(AirMissionState.LAUNCHING, mission.getState());
        Assert.assertEquals(SquadronState.QUEUED_FOR_MISSION, squadron.getState()); // Squadron is queued.

        mission.doAction(AirMissionAction.EXECUTE);                                 // Execute a turn.

        int distance = enemyAirfieldTarget.getDistance(airfield);
        int range = squadron.getRange(SquadronConfig.NONE);
        int eta = (distance / range) + (distance % range > 0 ? 1 : 0);

        int turnsToTarget = Deencapsulation.getField(mission, "turnsToTarget");

        final int targetEta = 2;
        Assert.assertEquals(targetEta, eta);
        Assert.assertEquals(eta, turnsToTarget);

        mission.doAction(AirMissionAction.RECALL);

        Assert.assertEquals(AirMissionState.IN_BOUND, mission.getState());          // Mission is in-bound.

        int turnsToHome = Deencapsulation.getField(mission, "turnsToHome");

        Assert.assertEquals(1, turnsToHome);                              // Mission should be able to return in a single turn since
                                                                                   // only a single turn has been executed.

        mission.doAction(AirMissionAction.EXECUTE);                                // Execute the new game turn.

        Assert.assertEquals(AirMissionState.DONE, mission.getState());             // Mission is done.
        Assert.assertEquals(airfield, squadron.getHome());                         // Squadron the same home.
        Assert.assertEquals(SquadronState.HANGER, squadron.getState());            // Squadron is in the hanger.
    }

    private Airfield buildAlexandriaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Alexandria");
        data.setSide(Side.ALLIES);
        data.setLandingType(List.of(LandingType.LAND, LandingType.SEAPLANE, LandingType.CARRIER));
        data.setMaxCapacity(20);
        data.setAntiAir(8);
        data.setLocation("BG32");

        return airfieldFactory.create(data);
    }

    private Airfield buildAboukirAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Aboukir");
        data.setSide(Side.ALLIES);
        data.setLandingType(List.of(LandingType.LAND, LandingType.SEAPLANE, LandingType.CARRIER));
        data.setMaxCapacity(20);
        data.setAntiAir(8);
        data.setLocation("BI32");

        return airfieldFactory.create(data);
    }

    private Airfield buildFamagustaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Famagusta");
        data.setSide(Side.ALLIES);
        data.setLandingType(List.of(LandingType.LAND, LandingType.SEAPLANE, LandingType.CARRIER));
        data.setMaxCapacity(20);
        data.setAntiAir(8);
        data.setLocation("BM25");

        return airfieldFactory.create(data);
    }

    private Airfield buildDernaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Derna");
        data.setSide(Side.AXIS);
        data.setLandingType(List.of(LandingType.LAND, LandingType.CARRIER));
        data.setMaxCapacity(8);
        data.setAntiAir(8);
        data.setLocation("AV29");

        return airfieldFactory.create(data);
    }

    private Airfield buildRhodesAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Rhodes");
        data.setSide(Side.AXIS);
        data.setLandingType(List.of(LandingType.LAND, LandingType.CARRIER));
        data.setMaxCapacity(8);
        data.setAntiAir(8);
        data.setLocation("BE23");

        return airfieldFactory.create(data);
    }

    private Region buildEgyptRegion() {
        RegionData regionData = new RegionData();
        regionData.setAirfields(List.of("Alexandria", "Aboukir"));
        regionData.setMin("0");
        regionData.setName("Egypt");
        regionData.setNation(Nation.BRITISH);

        return regionFactory.createLandRegion(Side.ALLIES, regionData);
    }

    private Squadron buildSunderlandSquadron() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Sunderland");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Squadron buildWellingtonSquadron() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Wellington");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Squadron buildHurricane() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Hurricane-1");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Target buildFriendlyAirfieldTarget(final Airfield airfield) {
        TargetData data = new TargetData();
        data.setType(TargetType.FRIENDLY_AIRBASE);
        data.setName(airfield.getName());
        data.setSide(Side.ALLIES);

        return targetFactory.createFriendlyAirfieldTarget(data);
    }

    private Target buildEnemyAirfield(final Airfield airfield) {
        TargetData data = new TargetData();
        data.setType(TargetType.ENEMY_AIRFIELD);
        data.setName(airfield.getName());
        data.setSide(Side.AXIS);

        return targetFactory.createEnemyAirfieldTarget(data);
    }

    private AirfieldView buildAirfieldView(final Airfield airfield) {
        AirfieldViewData data = new AirfieldViewData();
        data.setName(airfield.getName());
        data.setAirfield(airfield);
        return airfieldViewFactory.create(data);
    }

    private Map<MissionRole, List<String>> getSquadronMap(final List<Squadron> squadrons) {
        List<String> names = squadrons.stream().map(Squadron::getName).collect(Collectors.toList());

        Map<MissionRole, List<String>> map = new HashMap<>();
        MissionRole.stream().forEach(role -> map.put(role, new ArrayList<>()));
        map.put(MissionRole.MAIN, names);
        return map;
    }

    private static void initTurn() {
        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.TWILIGHT);
        scenario.setWeather(WeatherType.CLEAR);

        turn.start(scenario);
    }
}
