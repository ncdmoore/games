package enigma.waratsea.model.base.airfield.mission;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.AirfieldFactory;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.MissionSquadrons;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.map.region.data.RegionData;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.data.SquadronData;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MissionSquadronsTest {
    private static Injector injector;
    private static RegionFactory regionFactory;
    private static AirfieldFactory airfieldFactory;
    private static SquadronFactory squadronFactory;

    private static Airfield airfield;

    @BeforeClass
    public static void setup() {
        injector = Guice.createInjector(new TestModule());

        regionFactory = injector.getInstance(RegionFactory.class);
        airfieldFactory = injector.getInstance(AirfieldFactory.class);
        squadronFactory = injector.getInstance(SquadronFactory.class);
    }

    @Test
    public void testTurnAway() {
        MissionSquadrons missionSquadrons = injector.getInstance(MissionSquadrons.class);
        Region region = buildEgyptRegion();

        airfield = buildAlexandriaAirfield();

        airfield.addRegion(region);

        List<Squadron> squadrons = IntStream                            // Build 3 Wellington squadrons.
                .range(1, 4)
                .mapToObj(i -> buildWellingtonSquadron())
                .collect(Collectors.toList());

        squadrons.forEach(airfield::addSquadron);

        int initialSquadronSteps = squadrons                            // Squadrons are full strength.
                .stream()
                .map(Squadron::getSteps)
                .reduce(0, Integer::sum);

        List<String> squadronNames = squadrons
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        Map<MissionRole, List<String>> roleMap = new HashMap<>();
        roleMap.put(MissionRole.MAIN, squadronNames);

        missionSquadrons.setSquadrons(airfield, roleMap);

        // Make sure the mission has the correct number of squadrons.
        Assert.assertEquals(squadrons.size(), missionSquadrons.getNumber());

        int currentEffectiveStrength = getTotalMissionEffectiveStrength(squadrons);

        // Before the flak the mission's effective strength is equal to the initial number of steps.
        Assert.assertEquals(initialSquadronSteps, currentEffectiveStrength);

        // Turn away 3 steps.
        final int numStepsToTurnAway = 3;
        Deencapsulation.invoke(missionSquadrons, "turnAwayByFlak", numStepsToTurnAway);

        currentEffectiveStrength = getTotalMissionEffectiveStrength(squadrons);

        // The size of the mission's squadrons should be unchanged.
        Assert.assertEquals(squadrons.size(), missionSquadrons.getNumber());

        // The current effective strength is the initial number of steps minus the number of steps turned away.
        Assert.assertEquals(initialSquadronSteps - numStepsToTurnAway, currentEffectiveStrength);

        List<Squadron> turnedAway = missionSquadrons.getTurnedAway();

        int turnedAwayEffectiveStrength = getTotalMissionEffectiveStrength(turnedAway);   // The effective strength of the turned away squadrons.
        int turnedAwayStrength = getTotalMissionStrength(turnedAway);                     // The strength of the turned away squadrons.

        // The strength minus the remaining effective strength is now many steps were actually turned away.
        // This should equal the number of steps that were turned away.
        Assert.assertEquals(numStepsToTurnAway, turnedAwayStrength - turnedAwayEffectiveStrength);

        // Verify that the turnAwayByFlak method can be called multiple times.
        final int numStepsToTurnAway2 = 1;
        Deencapsulation.invoke(missionSquadrons, "turnAwayByFlak", numStepsToTurnAway2);

        // The squadrons on mission never change.
        Assert.assertEquals(squadrons.size(), missionSquadrons.getNumber());

        currentEffectiveStrength = getTotalMissionEffectiveStrength(squadrons);

        // The effective strength has been reduced by two separate calls to "turnAwayByFlak"
        Assert.assertEquals(initialSquadronSteps - (numStepsToTurnAway + numStepsToTurnAway2), currentEffectiveStrength);

        turnedAwayEffectiveStrength = getTotalMissionEffectiveStrength(turnedAway);
        turnedAwayStrength = getTotalMissionStrength(turnedAway);

        // The strength minus the remaining effective strength is now many steps were actually turned away.
        // This should equal the number of steps that were turned away.
        Assert.assertEquals(numStepsToTurnAway + numStepsToTurnAway2, turnedAwayStrength - turnedAwayEffectiveStrength);
    }

    @Test
    public void testDestroyedByFlak() {
        MissionSquadrons missionSquadrons = injector.getInstance(MissionSquadrons.class);

        Region region = buildEgyptRegion();

        airfield = buildAlexandriaAirfield();

        airfield.addRegion(region);

        List<Squadron> squadrons = IntStream                            // Build 3 Wellington squadrons.
                .range(1, 4)
                .mapToObj(i -> buildWellingtonSquadron())
                .collect(Collectors.toList());

        squadrons.forEach(airfield::addSquadron);

        List<String> names = squadrons
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        Map<MissionRole, List<String>> roleMap = new HashMap<>();
        roleMap.put(MissionRole.MAIN, names);

        missionSquadrons.setSquadrons(airfield, roleMap);

        final int numStepsToTurnAway = 3;
        Deencapsulation.invoke(missionSquadrons, "turnAwayByFlak", numStepsToTurnAway);

        int initialSquadronSteps = squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(0, Integer::sum);

        // The mission strength is not affected until the "destroyByFlak" call is made.
        // Verify the number of squadron pre "destroyByFlak" is correct.
        Assert.assertEquals(initialSquadronSteps, getTotalMissionStrength(squadrons));

        // The number of steps destroyed if half the number turned away per board game instructions.
        int numStepsDestroyed = numStepsToTurnAway / 2;

        Deencapsulation.invoke(missionSquadrons, "destroyByFlak", numStepsDestroyed);

        int strengthAfterFlak = initialSquadronSteps - numStepsDestroyed;

        // Verify that the strength of the mission after airbase flak is correct.
        Assert.assertEquals(strengthAfterFlak, getTotalMissionStrength(squadrons));
    }

    private static Region buildEgyptRegion() {
        RegionData regionData = new RegionData();
        regionData.setAirfields(List.of("Alexandria", "Aboukir"));
        regionData.setMin("0");
        regionData.setName("Egypt");
        regionData.setNation(Nation.BRITISH);

        return regionFactory.createLandRegion(Side.ALLIES, regionData);
    }

    private static Airfield buildAlexandriaAirfield() {
        AirfieldData data = new AirfieldData();
        data.setName("Alexandria");
        data.setSide(Side.ALLIES);
        data.setLandingType(List.of(LandingType.LAND, LandingType.SEAPLANE, LandingType.CARRIER));
        data.setMaxCapacity(20);
        data.setAntiAir(8);
        data.setLocation("BG32");

        return airfieldFactory.create(data);
    }

    private static Squadron buildWellingtonSquadron() {
        SquadronData data = new SquadronData();
        data.setModel("Wellington");
        data.setStrength(SquadronStrength.FULL);

        return squadronFactory.create(Side.ALLIES, Nation.BRITISH, data);
    }

    private int getTotalMissionEffectiveStrength(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .map(Squadron::getEffectiveStrength)
                .map(SquadronStrength::getSteps)
                .reduce(0, Integer::sum);
    }

    private int getTotalMissionStrength(final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .map(Squadron::getStrength)
                .map(SquadronStrength::getSteps)
                .reduce(0, Integer::sum);
    }
}
