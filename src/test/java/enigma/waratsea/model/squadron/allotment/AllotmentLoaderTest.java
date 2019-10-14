package enigma.waratsea.model.squadron.allotment;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Config;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.allotment.Allotment;
import engima.waratsea.model.squadron.allotment.AllotmentDAO;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;

public class AllotmentLoaderTest {
    private static AllotmentDAO allotmentDAO;
    private static Config config;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        config = injector.getInstance(Config.class);
        allotmentDAO = injector.getInstance(AllotmentDAO.class);

        gameTitle.setName(GameName.BOMB_ALLEY);
    }

    @Test
    public void testAllotmentLoading() throws Exception {
        Scenario scenario = new Scenario();
        scenario.setName("firstSortie");
        scenario.setTitle("The First Sortie");
        scenario.setMap("june1940");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1940, Calendar.JUNE, 11);
        scenario.setDate(calendar.getTime());

        config.setScenario(scenario.getName());

        Allotment alliedAllotment = allotmentDAO.load(scenario, Side.ALLIES, Nation.BRITISH);

        Assert.assertNotNull(alliedAllotment);
        Assert.assertFalse(alliedAllotment.getBombers().isEmpty());
        Assert.assertFalse(alliedAllotment.getFighters().isEmpty());
        Assert.assertFalse(alliedAllotment.getRecon().isEmpty());

        Allotment axisAllotment = allotmentDAO.load(scenario, Side.AXIS, Nation.ITALIAN);

        Assert.assertNotNull(axisAllotment);
        Assert.assertFalse(axisAllotment.getBombers().isEmpty());
        Assert.assertFalse(axisAllotment.getFighters().isEmpty());
        Assert.assertFalse(axisAllotment.getRecon().isEmpty());
    }
}
