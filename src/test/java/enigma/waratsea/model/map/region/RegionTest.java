package enigma.waratsea.model.map.region;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionDAO;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioDAO;
import enigma.waratsea.TestModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

@Slf4j
public class RegionTest {
    private static GameTitle gameTitle;
    private static ScenarioDAO scenarioDAO;
    private static RegionDAO regionDAO;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);                                                              //The game instance must be injected first!
        scenarioDAO = injector.getInstance(ScenarioDAO.class);
        regionDAO = injector.getInstance(RegionDAO.class);
    }

    @Test
    public void testMapLoading() throws Exception {
        mapLoading("bombAlley");
    }


    private void mapLoading(String gameName) throws Exception {

        gameTitle.setValue(gameName);

        List<Scenario> scenarios = scenarioDAO.load();
        scenarios.forEach(this::loadRegion);
    }

    private void loadRegion(Scenario scenario) {

        //todo remove once all maps are setup.
        if (scenario.getMap() == null) {
            log.error("Scenario '{}' needs a map", scenario.getTitle());
            return;
        }

        try {
            List<Region> regions = regionDAO.loadRegions(scenario, Side.ALLIES);

            Assert.assertFalse(regions.isEmpty());

            regions = regionDAO.loadRegions(scenario, Side.AXIS);

            Assert.assertFalse(regions.isEmpty());
        } catch (Exception ex) {
            assert false;
        }
    }
}
