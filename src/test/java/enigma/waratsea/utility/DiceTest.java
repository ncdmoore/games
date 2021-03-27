package enigma.waratsea.utility;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.utility.Dice;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DiceTest {
    private Dice dice;

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        dice = injector.getInstance(Dice.class);                                                    //The game instance must be injected first!
    }

    @Test
    public void testRandomList() {
        int result = dice.sumDiceRoll(0);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testRoll6() {
        int result = dice.roll();
        Assert.assertTrue(result <= 6);
        Assert.assertTrue(result >= 1);
    }

    @Test
    public void testRoll6Twice() {
        int result = dice.sumDiceRoll(2);
        Assert.assertTrue(result <= 12);
        Assert.assertTrue(result >= 2);
    }

    @Test
    public void testBinomial() {
        int result = dice.probabilityHitsPercentage(1, 1,1.0 / 6.0);
        Assert.assertEquals(16, result);

        result = dice.probabilityHitsPercentage(3, 6, 1.0 / 6.0);
        Assert.assertEquals(6, result);
    }
}
