package engima.waratsea;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import engima.waratsea.model.player.ComputerPlayer;
import engima.waratsea.model.player.HumanPlayer;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.model.ships.TaskForceFactory;

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
    }
}


