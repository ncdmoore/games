package enigma.waratsea.model.squadron;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameName;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronFactory;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.data.SquadronData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SquadronTest {

    private static GameTitle gameTitle;
    private static SquadronFactory factory;


    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setName(GameName.BOMB_ALLEY);
        factory = injector.getInstance(SquadronFactory.class);
    }

    @Test
    public void bombAlleyAswFalseTest() {

        SquadronData data = new SquadronData();
        data.setName("JG2");
        data.setModel("Bf109e");
        data.setStrength(SquadronStrength.FULL);

        Squadron squadron = factory.create(Side.AXIS, Nation.GERMAN, data);

        Assert.assertFalse(squadron.canDoASW());
    }

    @Test
    public void bombAlleyAswTrueTest() {

        SquadronData data = new SquadronData();
        data.setName("RAF");
        data.setModel("Sunderland");
        data.setStrength(SquadronStrength.FULL);

        Squadron squadron = factory.create(Side.ALLIES, Nation.BRITISH, data);

        Assert.assertTrue(squadron.canDoASW());
    }

    @Test
    public void coralSeaAswTest() {
        gameTitle.setName(GameName.CORAL_SEA);

        SquadronData data = new SquadronData();
        data.setName("IJN");
        data.setModel("B5N2");
        data.setStrength(SquadronStrength.FULL);

        Squadron squadron = factory.create(Side.AXIS, Nation.JAPANESE, data);

        Assert.assertTrue(squadron.canDoASW());

    }
}
