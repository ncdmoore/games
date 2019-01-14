package enigma.waratsea.model.victory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.ship.ShipEventAction;
import engima.waratsea.model.game.event.ship.data.ShipMatchData;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.ShipId;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.ships.Shipyard;
import engima.waratsea.model.ships.SurfaceShip;
import engima.waratsea.model.victory.Victory;
import engima.waratsea.model.victory.data.ShipVictoryData;
import engima.waratsea.model.victory.data.VictoryData;
import enigma.waratsea.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VictoryTest {

    private static Ship battleShip;

    @BeforeClass
    public static void setup() throws Exception {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        Shipyard shipyard = injector.getInstance(Shipyard.class);

        String shipName = "BB08 Royal Sovereign";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);
        battleShip = shipyard.build(shipId);
    }

    @Test
    public void testShipEvent() {

        int victoryPoints = 3;

        ShipMatchData shipMatchData = new ShipMatchData();
        shipMatchData.setAction("DAMAGED_PRIMARY");
        shipMatchData.setShipType("BATTLESHIP");

        ShipVictoryData shipVictoryData = new ShipVictoryData();
        shipVictoryData.setEvent(shipMatchData);
        shipVictoryData.setPoints(victoryPoints);

        List<ShipVictoryData> shipData = new ArrayList<>();
        shipData.add(shipVictoryData);


        VictoryData victoryData = new VictoryData();
        victoryData.setShip(shipData);

        Victory victory = new Victory(victoryData, Side.ALLIES);

        Assert.assertEquals(0, victory.getTotalVictoryPoints());

        ShipEvent event = new ShipEvent();
        event.setAction(ShipEventAction.DAMAGED_PRIMARY);
        event.setShip(battleShip);

        event.fire();

        Assert.assertEquals(victoryPoints, victory.getTotalVictoryPoints());

    }
}
