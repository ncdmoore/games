package enigma.waratsea.model.game.rules;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.airfield.AirbaseType;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.rules.SquadronConfigRules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.squadron.SquadronConfig;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

public class SquadronConfigRulesTest {

    private SquadronConfigRules rules;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        rules = injector.getInstance(SquadronConfigRules.class);
    }

    @Test
    public void testSquadronConfigDropTanks() {
        // Test a fighter on escort. Drop tanks are allowed.
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setMissionRole(MissionRole.ESCORT);

        Set<SquadronConfig> allowed = rules.getAllowed(dto);

        Assert.assertEquals(2, allowed.size());
        Assert.assertTrue(allowed.contains(SquadronConfig.NONE));
        Assert.assertTrue(allowed.contains(SquadronConfig.DROP_TANKS));
        Assert.assertFalse(allowed.contains(SquadronConfig.STRIPPED_DOWN));
        Assert.assertFalse(allowed.contains(SquadronConfig.SEARCH));

        // Test a fighter performing the main role. No drop tanks are allowed.
        dto = new SquadronConfigRulesDTO()
                .setMissionRole(MissionRole.MAIN);

        allowed = rules.getAllowed(dto);

        Assert.assertFalse(allowed.contains(SquadronConfig.DROP_TANKS));
    }

    @Test
    public void testSquadronConfigStrippedDown() {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setAirfieldType(AirbaseType.CARRIER)
                .setMissionType(AirMissionType.FERRY);

        Set<SquadronConfig> allowed = rules.getAllowed(dto);

        Assert.assertTrue(allowed.contains(SquadronConfig.STRIPPED_DOWN));
    }

    @Test
    public void testSquadronConfigLeanEngine() {
        // Squadrons on ferry missions are allowed to configure a lean engine.
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setMissionType(AirMissionType.FERRY);

        Set<SquadronConfig> allowed = rules.getAllowed(dto);

        Assert.assertTrue(allowed.contains(SquadronConfig.LEAN_ENGINE));

        // Squadrons on land strike missions are allowed to configure a lean engine.
        dto = new SquadronConfigRulesDTO()
                .setMissionType(AirMissionType.LAND_STRIKE);

        allowed = rules.getAllowed(dto);

        Assert.assertTrue(allowed.contains(SquadronConfig.LEAN_ENGINE));

        // Squadrons on naval strike missions are not allowed to configure a lean engine. This is a game rule!
        dto = new SquadronConfigRulesDTO()
                .setMissionType(AirMissionType.NAVAL_PORT_STRIKE);

        allowed = rules.getAllowed(dto);

        Assert.assertFalse(allowed.contains(SquadronConfig.LEAN_ENGINE));
    }

    @Test
    public void testSquadronConfigSearch() {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setPatrolType(PatrolType.SEARCH);

        Set<SquadronConfig> allowed = rules.getAllowed(dto);

        Assert.assertTrue(allowed.contains(SquadronConfig.SEARCH));
    }

    @Test
    public void testSquadronConfigReducedPayload() {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO();
        Set<SquadronConfig> allowed = rules.getAllowed(dto);

        Assert.assertFalse(allowed.contains(SquadronConfig.REDUCED_PAYLOAD));
    }
}
