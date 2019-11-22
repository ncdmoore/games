package enigma.waratsea.model.base.airfield.patrol;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolFactory;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.squadron.Squadron;
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

public class PatrolTest {

    private static GameTitle gameTitle;
    private static PatrolFactory patrolFactory;
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
        patrolFactory = injector.getInstance(PatrolFactory.class);

        Weather weather = injector.getInstance(Weather.class);
        weather.setCurrent(WeatherType.CLEAR);
    }

    @Test
    public void airSearchPatrolTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        Airfield airfield = buildAirfield();

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        PatrolData data = new PatrolData();
        data.setAirbase(airfield);
        data.setSquadrons(Collections.singletonList(squadron.getName()));

        Patrol patrol = patrolFactory.createSearch(data);

        int rate = patrol.getSuccessRate(squadron.getRadius().stream().max(Integer::compare).orElse(0));

        Assert.assertEquals(0, rate);

        rate = patrol.getSuccessRate(3);

        Assert.assertEquals(33, rate);
    }

    @Test
    public void airSearchPatrolTrueMaxRadiusTest() {
        gameTitle.setName(GameName.BOMB_ALLEY);

        Region region = buildRegion();

        Airfield airfield = buildAirfield();

        airfield.addRegion(region);

        Squadron squadron = buildSquadronSeaplaneRecon();

        airfield.addSquadron(squadron);

        PatrolData data = new PatrolData();
        data.setAirbase(airfield);
        data.setSquadrons(Collections.singletonList(squadron.getName()));

        Patrol patrol = patrolFactory.createSearch(data);

        int maxRadius = patrol.getTrueMaxRadius();

        Assert.assertEquals(6, maxRadius);


    }

    private Squadron buildSquadronSeaplaneRecon() {
        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Sunderland");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private Airfield buildAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Gibraltar");
        data.setSide(Side.ALLIES);
        data.setLandingType(new ArrayList<>(Arrays.asList(LandingType.LAND, LandingType.SEAPLANE)));
        data.setMaxCapacity(40);
        data.setAntiAir(6);
        data.setLocation("G20");

        return airfieldFactory.create(data);
    }

    private Region buildRegion() {
        RegionData regionData = new RegionData();
        regionData.setAirfields(new ArrayList<>(Collections.singletonList("Gibraltar")));
        regionData.setMin("20");
        regionData.setName("Gibraltar");
        regionData.setNation(new ArrayList<>(Collections.singletonList(Nation.BRITISH)));

        return regionFactory.create(Side.ALLIES, regionData);
    }
}
