package enigma.waratsea;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.event.DateEvent;
import engima.waratsea.event.DateEventFactory;
import engima.waratsea.event.GameEvent;
import engima.waratsea.event.ShipEvent;
import engima.waratsea.event.ShipEventFactory;
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
import engima.waratsea.model.player.Player;
import enigma.waratsea.event.TestEvent;
import enigma.waratsea.event.TestEventFactory;

public class TestModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Player.class).annotatedWith(Names.named("Human")).to(HumanPlayer.class);
        bind(Player.class).annotatedWith(Names.named("Computer")).to(ComputerPlayer.class);

        //Install ship event factory. This allows guice to help construct ship events.
        install(new FactoryModuleBuilder()
                .implement(GameEvent.class, ShipEvent.class)
                .build(ShipEventFactory.class));

        //Install date event factory. This allows guice to help construct dates.
        install(new FactoryModuleBuilder()
                .implement(GameEvent.class, DateEvent.class)
                .build(DateEventFactory.class));

        //Install date event factory. This allows guice to help construct dates.
        install(new FactoryModuleBuilder()
                .implement(GameEvent.class, TestEvent.class)
                .build(TestEventFactory.class));
    }
}
