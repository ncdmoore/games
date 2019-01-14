package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.victory.ShipVictory;
import engima.waratsea.model.victory.Victory;
import engima.waratsea.model.victory.VictoryLoader;
import enigma.waratsea.TestModule;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class VictoryLoaderTest {
    private static VictoryLoader loader;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());
        loader = injector.getInstance(VictoryLoader.class);
    }

    @Test
    public void testDefaultVictoryLoading() throws Exception {
        Victory alliedVictory = loader.build(Side.ALLIES);
        Victory axisVictory = loader.build(Side.AXIS);

        List<ShipVictory> alliedShips = Deencapsulation.getField(alliedVictory, "defaultShips");

        Assert.assertNotNull(alliedShips);
        Assert.assertFalse(alliedShips.isEmpty());

        List<ShipVictory> axisShips = Deencapsulation.getField(axisVictory, "defaultShips");

        Assert.assertNotNull(axisShips);
        Assert.assertFalse(axisShips.isEmpty());

        List<ShipEventMatcher> alliedMatchers = getMatchers(alliedShips);

        Assert.assertNotNull(alliedMatchers);
        Assert.assertFalse(alliedMatchers.isEmpty());
        Assert.assertEquals(1, alliedMatchers.size());

        List<ShipEventMatcher> axisMatchers = getMatchers(axisShips);

        Assert.assertNotNull(axisMatchers);
        Assert.assertFalse(axisMatchers.isEmpty());
        Assert.assertEquals(1, axisMatchers.size());
    }

    private List<ShipEventMatcher> getMatchers(final List<ShipVictory> shipVictory) {
        return shipVictory.stream()
                .map(victory -> (ShipEventMatcher) Deencapsulation.getField(victory, "matcher"))
                .filter(matcher -> matcher.getAction().equalsIgnoreCase("SUNK"))
                .collect(toList());
    }
}
