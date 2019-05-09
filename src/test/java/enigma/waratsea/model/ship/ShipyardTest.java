package enigma.waratsea.model.ship;

import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.data.ShipData;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.Shipyard;
import enigma.waratsea.TestModule;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShipyardTest {

    private static Shipyard shipyard;

    @BeforeClass
    public static void setup() {
        Injector injector = Guice.createInjector(new TestModule());

        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue("bombAlley");

        shipyard = injector.getInstance(Shipyard.class);
    }

    @Test
    public void testShipClassLoadForAircraftCarrier() {
        String shipClass = "Eagle-1";
        ShipData shipData = Deencapsulation.invoke(shipyard, "loadNewShipData", shipClass, new ShipId("CVL04 Eagle-1", Side.ALLIES));
        assert (shipData.getType().equals(ShipType.AIRCRAFT_CARRIER));
    }

    @Test
    public void testShipClassLoadForDestroyer() {
        String shipClass = "A Class";
        ShipData shipData = Deencapsulation.invoke(shipyard, "loadNewShipData", shipClass, new ShipId("DD15 Active", Side.ALLIES));
        assert (shipData.getType().equals(ShipType.DESTROYER));
    }

    @Test
    public void testBuildAircraftCarrier() throws Exception {
        testBuildAircraftCarrier("CVL01 Argus-1", Nation.BRITISH, 3);
        testBuildAircraftCarrier("CV04 Ark Royal-1", Nation.BRITISH, 9);
        testBuildAircraftCarrier("CVL04 Eagle-1", Nation.BRITISH, 4);
        testBuildAircraftCarrier("CV01 Furious-1", Nation.BRITISH, 6);
        testBuildAircraftCarrier("CV05 Illustrious-1", Nation.BRITISH, 6);
        testBuildAircraftCarrier("CV08 Indomitable-1", Nation.BRITISH, 8);
    }

    @Test
    public void testBuildSeaPlaneCarrier() throws Exception {
        testBuildSeaPlaneCarrier("CVS01 Commandant Teste", Nation.FRENCH, 5);
    }

    @Test
    public void testBuildBattleship() throws Exception {
        testBuildBattleship("BB04 Barham", Nation.BRITISH);
        testBuildBattleship("BB03 Bretagne", Nation.FRENCH);
        testBuildBattleship("BB13 Prince of Wales", Nation.BRITISH);
        testBuildBattleship("AG01 Centurion", Nation.BRITISH);
        testBuildBattleship("BB05 Malaya", Nation.BRITISH);
        testBuildBattleship("BB11 Nelson", Nation.BRITISH);
        testBuildBattleship("BB03 Valiant", Nation.BRITISH);
        testBuildBattleship("BB07 Resolution", Nation.BRITISH);
        testBuildBattleship("BB02 Warspite", Nation.BRITISH);
    }

    @Test
    public void testBuildBattlecruiser() throws Exception {
        testBuildBattleCruiser("BC01 Dunkerque", Nation.FRENCH);
        testBuildBattleCruiser("BC03 Hood", Nation.BRITISH);
        testBuildBattleCruiser("BC01 Renown", Nation.BRITISH);
        testBuildBattleCruiser("BC02 Repulse", Nation.BRITISH);
    }

    @Test
    public void testBuildCruiser() throws Exception {
        testBuildCruiser("CL29 Arethusa", Nation.BRITISH);
        testBuildCruiser("CL33 Birmingham", Nation.BRITISH);
        testBuildCruiser("CL06 Coventry", Nation.BRITISH);
        testBuildCruiser("CL02 Calypso", Nation.BRITISH);
        testBuildCruiser("CL01b Caledon", Nation.BRITISH);
        testBuildCruiser("CL12 Capetown", Nation.BRITISH);
        testBuildCruiser("CL45 Charybdis", Nation.BRITISH);
        testBuildCruiser("CL46 Cleopatra", Nation.BRITISH);
        testBuildCruiser("CA01 Berwick", Nation.BRITISH);
        testBuildCruiser("CL56 Fiji", Nation.BRITISH);
        testBuildCruiser("CL16a Delhi", Nation.BRITISH);
        testBuildCruiser("CL16b Delhi", Nation.BRITISH);
        testBuildCruiser("CL51 Phoebe", Nation.BRITISH);
        testBuildCruiser("CL01 Duguay Trouin", Nation.FRENCH);
        testBuildCruiser("CA01 Duquesne", Nation.FRENCH);
        testBuildCruiser("CL42 Edinburgh", Nation.BRITISH);
        testBuildCruiser("CL23 Enterprise", Nation.BRITISH);
        testBuildCruiser("CL48 Euryalus", Nation.BRITISH);
        testBuildCruiser("CL38 Gloucester", Nation.BRITISH);
        testBuildCruiser("CA05 Kent", Nation.BRITISH);
        testBuildCruiser("CL09 Gloire", Nation.FRENCH);
        testBuildCruiser("CL25 Ajax", Nation.BRITISH);
        testBuildCruiser("CL01 Perth", Nation.AUSTRALIAN);
        testBuildCruiser("CL62 Nigeria", Nation.BRITISH);
        testBuildCruiser("CL34 Glasgow", Nation.BRITISH);
        testBuildCruiser("CA03 Suffren", Nation.FRENCH);
        testBuildCruiser("CA12 York", Nation.BRITISH);
    }

    @Test
    public void testBuildDestroyer() throws Exception {
        testBuildDestroyer("DD18 Arrow", Nation.BRITISH);
        testBuildDestroyer("DD03 Lynx", Nation.FRENCH);
        testBuildDestroyer("DD34 Duncan", Nation.BRITISH);
        testBuildDestroyer("DD43 Echo", Nation.BRITISH);
        testBuildDestroyer("DD54 Fearless", Nation.BRITISH);
        testBuildDestroyer("DD61 Gallant", Nation.BRITISH);
        testBuildDestroyer("DD07 Garland", Nation.POLISH);
        testBuildDestroyer("DD69 Hasty", Nation.BRITISH);
        testBuildDestroyer("DD77 Icarus", Nation.BRITISH);
        testBuildDestroyer("DD112 Jackal", Nation.BRITISH);
        testBuildDestroyer("DD120 Kandahar", Nation.BRITISH);
        testBuildDestroyer("DD49 Le Fortune", Nation.FRENCH);
        testBuildDestroyer("DD130 Laforey", Nation.BRITISH);
        testBuildDestroyer("DD25 Le Fantasque", Nation.FRENCH);
        testBuildDestroyer("DD139 Marne", Nation.BRITISH);
        testBuildDestroyer("DD31 Mogador", Nation.FRENCH);
        testBuildDestroyer("DD05 Napier", Nation.AUSTRALIAN);
        testBuildDestroyer("DD149 Onslow", Nation.BRITISH);
        testBuildDestroyer("DD153 Pakenham", Nation.BRITISH);
        testBuildDestroyer("DD165 Quentin", Nation.BRITISH);
        testBuildDestroyer("DD06 Douglas", Nation.BRITISH);
        testBuildDestroyer("DD01 Stuart", Nation.AUSTRALIAN);
        testBuildDestroyer("DD02 Keppel", Nation.BRITISH);
        testBuildDestroyer("DD97 Ashanti", Nation.BRITISH);
        testBuildDestroyer("DD22 Kersaint", Nation.FRENCH);
        testBuildDestroyer("DD31 Mogador", Nation.FRENCH);
        testBuildDestroyer("DD32 Volta", Nation.FRENCH);

    }

    @Test
    public void testBuildDestroyerEscort() throws Exception {
        testBuildDestroyerEscort("DE61 Airedale-1", Nation.BRITISH);
        testBuildDestroyerEscort("DE85 Bittern-1", Nation.BRITISH);
        testBuildDestroyerEscort("DE97 Flower-1", Nation.BRITISH);
        testBuildDestroyerEscort("DE25 Hunt-1", Nation.BRITISH);
        testBuildDestroyerEscort("DE01 V&W-1", Nation.BRITISH);
        testBuildDestroyerEscort("DE25 V&W-25", Nation.AUSTRALIAN);
        testBuildDestroyerEscort("DE01 Yarra-1", Nation.AUSTRALIAN);
    }

    @Test
    public void testBuildMineLayer() throws Exception {
        String shipName = "ML02 Abdiel";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);


        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.MINELAYER, ship.getType());
        Assert.assertEquals(Nation.BRITISH, ship.getNationality());
    }

    @Test
    public void testBuildMineSweeper() throws Exception {
        String shipName = "MS01 Halcyon-1";
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);


        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.MINESWEEPER, ship.getType());
        Assert.assertEquals(Nation.BRITISH, ship.getNationality());
    }

    @Test
    public void testBuildTransport() throws Exception {
        testBuildTransport("TR01", Nation.BRITISH);
        testBuildTransport("TR13", Nation.BRITISH);
    }

    @Test
    public void testBuildOiler() throws Exception {
        testBuildOiler("AO03", Nation.BRITISH);
    }

    private void testBuildAircraftCarrier(final String shipName, final Nation nation, final int capacity) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.AIRCRAFT_CARRIER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());

        Airbase carrier = (Airbase) ship;
        Assert.assertEquals(capacity, carrier.getCapacity());
    }

    private void testBuildSeaPlaneCarrier(final String shipName, final Nation nation, final int capacity) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.SEAPLANE_CARRIER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());

        Airbase seaplane = (Airbase) ship;
        Assert.assertEquals(capacity, seaplane.getCapacity());
    }

    private void testBuildBattleship(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.BATTLESHIP, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildBattleCruiser(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.BATTLECRUISER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildCruiser(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.CRUISER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildDestroyer(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.DESTROYER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildDestroyerEscort(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.DESTROYER_ESCORT, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildTransport(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.TRANSPORT, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }

    private void testBuildOiler(final String shipName, final Nation nation) throws Exception {
        ShipId shipId = new ShipId(shipName, Side.ALLIES);

        Ship ship = shipyard.load(shipId);

        Assert.assertNotNull(ship);
        Assert.assertEquals(ShipType.OILER, ship.getType());
        Assert.assertEquals(nation, ship.getNationality());
    }
}
