package enigma.waratsea;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.ships.AircraftCarrier;
import engima.waratsea.model.ships.AircraftCarrierFactory;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.SurfaceShip;
import engima.waratsea.model.ships.SurfaceShipFactory;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.model.taskForce.TaskForceFactory;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Names.named("Human")).to(HumanPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Computer")).to(ComputerPlayer.class);

        install(new FactoryModuleBuilder().implement(TaskForce.class, TaskForce.class).build(TaskForceFactory.class));
        install(new FactoryModuleBuilder().implement(Ship.class, AircraftCarrier.class).build(AircraftCarrierFactory.class));
        install(new FactoryModuleBuilder().implement(Ship.class, SurfaceShip.class).build(SurfaceShipFactory.class));
    }
}
