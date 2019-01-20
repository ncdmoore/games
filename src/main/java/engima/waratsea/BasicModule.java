package engima.waratsea;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.airfield.AirfieldFactory;
import engima.waratsea.model.game.event.ship.ShipEventMatcher;
import engima.waratsea.model.game.event.ship.ShipEventMatcherFactory;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionFactory;
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.port.Port;
import engima.waratsea.model.port.PortFactory;
import engima.waratsea.model.ships.AircraftCarrier;
import engima.waratsea.model.ships.AircraftCarrierFactory;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.SurfaceShip;
import engima.waratsea.model.ships.SurfaceShipFactory;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;
import engima.waratsea.model.victory.ShipVictory;
import engima.waratsea.model.victory.ShipVictoryFactory;
import engima.waratsea.model.victory.VictoryConditions;
import engima.waratsea.model.victory.VictoryConditionsFactory;

/**
 * The guice basic module that configures the bindings for the main application.
 */
public class BasicModule extends AbstractModule {

    /**
     * Configure the main application guice model.
     */
    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Names.named("Human")).to(HumanPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Computer")).to(ComputerPlayer.class);

        install(new FactoryModuleBuilder().implement(TaskForce.class, TaskForce.class).build(TaskForceFactory.class));
        install(new FactoryModuleBuilder().implement(Ship.class, AircraftCarrier.class).build(AircraftCarrierFactory.class));
        install(new FactoryModuleBuilder().implement(Ship.class, SurfaceShip.class).build(SurfaceShipFactory.class));

        install(new FactoryModuleBuilder().implement(Region.class, Region.class).build(RegionFactory.class));

        install(new FactoryModuleBuilder().implement(VictoryConditions.class, VictoryConditions.class).build(VictoryConditionsFactory.class));
        install(new FactoryModuleBuilder().implement(ShipVictory.class, ShipVictory.class).build(ShipVictoryFactory.class));

        install(new FactoryModuleBuilder().implement(Airfield.class, Airfield.class).build(AirfieldFactory.class));
        install(new FactoryModuleBuilder().implement(Port.class, Port.class).build(PortFactory.class));

        install(new FactoryModuleBuilder().implement(ShipEventMatcher.class, ShipEventMatcher.class).build(ShipEventMatcherFactory.class));

    }
}


