package enigma.waratsea.model.target;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.ship.Shipyard;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForceFactory;
import enigma.waratsea.TestModule;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TargetTest {
    private static GameMap gameMap;
    private static Game game;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);

        game = injector.getInstance(Game.class);

        List<Scenario> scenarios = game.initScenarios();
        game.setScenario(scenarios.get(0));
        game.setHumanSide(Side.ALLIES);
        game.startNew();
    }

    @Test
    public void testTargetEquals() {

        List<Target> airfieldTargets = game.getHumanPlayer().getEnemyAirfieldTargets();

        Target targetOne = airfieldTargets.get(0);
        Target targetTwo = airfieldTargets.get(0);

        assertTrue(targetOne.isEqual(targetTwo));

        List<Target> portTargets = game.getHumanPlayer().getEnemyPortTargets();

        Target targetThree = portTargets.get(0);

        assertFalse(targetOne.isEqual(targetThree));
    }

}
