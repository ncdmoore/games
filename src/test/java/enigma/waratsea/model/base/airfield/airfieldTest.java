package enigma.waratsea.model.base.airfield;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Turn;
import engima.waratsea.model.game.TurnIndex;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.data.SquadronData;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.model.weather.WeatherType;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class airfieldTest {

    private static GameTitle gameTitle;
    private static AirfieldFactory airfieldFactory;
    private static SquadronFactory squadronFactory;
    private static RegionFactory regionFactory;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);
        airfieldFactory = injector.getInstance(AirfieldFactory.class);
        squadronFactory = injector.getInstance(SquadronFactory.class);
        regionFactory = injector.getInstance(RegionFactory.class);

        Scenario scenario = new Scenario();
        scenario.setDate(new Date());
        scenario.setTurnIndex(TurnIndex.DAY_1);
        scenario.setWeather(WeatherType.CLEAR);

        Weather weather = injector.getInstance(Weather.class);
        weather.setCurrent(WeatherType.CLEAR);

        Turn turn = injector.getInstance(Turn.class);

        turn.start(scenario);
    }

    @Test
    public void addSquadronToSearchTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.SEARCH).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.SEARCH).getAssignedSquadrons(Nation.BRITISH);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertTrue(result);
    }

    @Test
    public void addSquadronToCAPTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.LAND)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronLandFighter();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.CAP).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.CAP).getAssignedSquadrons(Nation.BRITISH);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertTrue(result);
    }

    @Test
    public void addSquadronToASWTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.ASW).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.ASW).getAssignedSquadrons(Nation.BRITISH);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertTrue(result);
    }

    @Test
    public void addInvalidSquadronToCAPTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.CAP).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.CAP).getAssignedSquadrons(Nation.BRITISH);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertFalse(result);   // Recon squadrons cannot do CAP.
    }

    @Test
    public void addInvalidSquadronToASWTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.LAND)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronLandFighter();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.ASW).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.ASW).getAssignedSquadrons(Nation.BRITISH);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertFalse(result);   // Fighter squadrons cannot do ASW.
    }

    @Test
    public void addSquadronToASWTestCoralSea() {
        gameTitle.setName(GameName.CORAL_SEA);

        Region region = buildRegionCoralSea();

        AirfieldData data = new AirfieldData();
        data.setName("Cairns");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.LAND)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronLandBomber();

        airfield.addSquadron(squadron);

        airfield.getPatrol(PatrolType.ASW).addSquadron(squadron);

        List<Squadron> searchSquadrons = airfield.getPatrol(PatrolType.ASW).getAssignedSquadrons(Nation.UNITED_STATES);

        boolean result = searchSquadrons.contains(squadron);

        Assert.assertTrue(result);   // Bomber squadrons can do ASW in Coral Sea.
    }

    @Test
    public void addSquadronToAirfieldTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        List<Squadron> squadrons = airfield.getSquadrons(Nation.BRITISH);

        boolean result = squadrons.contains(squadron);

        Assert.assertTrue(result);
    }


    @Test
    public void addSquadronToAirfieldInvalidLandingTypeTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Collections.singletonList(LandingType.LAND)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        List<Squadron> squadrons = airfield.getSquadrons(Nation.BRITISH);

        boolean result = squadrons.contains(squadron);

        Assert.assertFalse(result);  // Attempt to add a seaplane to an airfield that only supports land squadrons.
    }

    @Test
    public void maxSearchRadiusTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Arrays.asList(LandingType.LAND, LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron reconSquadron = buildSquadronSeaplaneRecon();
        Squadron fighterSquadron = buildSquadronLandFighter();

        airfield.addSquadron(reconSquadron);
        airfield.addSquadron(fighterSquadron);

        airfield.getPatrol(PatrolType.SEARCH).addSquadron(fighterSquadron);
        airfield.getPatrol(PatrolType.SEARCH).addSquadron(reconSquadron);

        int searchRadius = airfield.getPatrol(PatrolType.SEARCH).getMaxRadius();
        int reconRadius = reconSquadron.getRadius(SquadronConfig.SEARCH);

        // Recon plane's radius should be the search's maximum radius.
        Assert.assertEquals(reconRadius, searchRadius);

        airfield.getPatrol(PatrolType.SEARCH).removeSquadron(reconSquadron);

        int fighterRadius = fighterSquadron.getRadius(SquadronConfig.SEARCH);
        searchRadius = airfield.getPatrol(PatrolType.SEARCH).getMaxRadius();

        Assert.assertEquals(fighterRadius, searchRadius);
    }

    @Test
    public void radiiMapTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Arrays.asList(LandingType.LAND, LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        Squadron reconSquadron = buildSquadronSeaplaneRecon();
        Squadron fighterSquadron = buildSquadronLandFighter();

        airfield.addSquadron(reconSquadron);
        airfield.addSquadron(fighterSquadron);

        airfield.getPatrol(PatrolType.SEARCH).addSquadron(fighterSquadron);
        airfield.getPatrol(PatrolType.SEARCH).addSquadron(reconSquadron);
        airfield.getPatrol(PatrolType.ASW).addSquadron(reconSquadron);

        Map<Integer, List<Patrol>> radiiMap = airfield.getPatrolRadiiMap();

        Set<Integer> radii = radiiMap.keySet();

        boolean result = radii.contains(airfield.getPatrol(PatrolType.SEARCH).getTrueMaxRadius());

        Assert.assertTrue(result);

        airfield.getPatrol(PatrolType.CAP).addSquadron(fighterSquadron);

        radiiMap = airfield.getPatrolRadiiMap();

        radii = radiiMap.keySet();

        result = radii.contains(airfield.getPatrol(PatrolType.CAP).getTrueMaxRadius());

        Assert.assertTrue(result);  // Fighter is on CAP so its radius should be in the map.
    }

    @Test
    public void airOptsStatsTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        List<LandingType> landingTypes = new ArrayList<>(Arrays.asList(LandingType.LAND, LandingType.SEAPLANE));

        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(landingTypes);
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        Airfield airfield = airfieldFactory.create(data);

        airfield.addRegion(region);

        List<ProbabilityStats> stats = airfield.getAirOperationStats();

        Assert.assertEquals(landingTypes.size(), stats.size());
    }

        private Region buildRegion() {
        RegionData regionData = new RegionData();
        regionData.setAirfields(new ArrayList<>(Collections.singletonList("Gibraltar")));
        regionData.setMin("20");
        regionData.setName("Gibraltar");
        regionData.setNation(Nation.BRITISH);

        return regionFactory.create(Side.ALLIES, regionData);
    }

    private Region buildRegionCoralSea() {
        RegionData regionData = new RegionData();
        regionData.setAirfields(new ArrayList<>(Collections.singletonList("Cairns")));
        regionData.setMin("30");
        regionData.setName("Australia");
        regionData.setNation(Nation.UNITED_STATES);

        return regionFactory.create(Side.ALLIES, regionData);
    }

    private Squadron buildSquadronLandFighter() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Kittyhawk");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Squadron buildSquadronSeaplaneRecon() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Sunderland");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Squadron buildSquadronLandBomber() {
        SquadronData data = new SquadronData();
        data.setName("AA");
        data.setModel("B25A");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.UNITED_STATES, data);
    }
}
