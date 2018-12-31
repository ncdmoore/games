package engima.waratsea;


import com.google.inject.Guice;
import com.google.inject.Injector;
import engima.waratsea.model.game.GameTitle;
import javafx.application.Application;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.presenter.StartPresenter;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This is the main application class for World War Two At Sea.
 */
@Slf4j
public class Main extends Application {

    private static final int NAME = 0;
    private static final int VALUE = 1;
    private static final String APPLICATION_NAME = "World War Two At Sea";
    private static final String GAME = "game";

    private static final Map<String, String> PARAMETERS = new HashMap<>();
    private static final Map<String, Consumer<String>> HANDLERS = new HashMap<>();

    static {
        PARAMETERS.put(GAME, GameTitle.DEFAULT_GAME);
        HANDLERS.put(GAME, Main::handleGameParameter);
    }

    /**
     * This is the entry point into the javafx GUI.
     * @param primaryStage The primary javafx stage of the application.
     */
    @Override
    public void start(final Stage primaryStage) {
        primaryStage.setTitle(APPLICATION_NAME);

        Injector injector = Guice.createInjector(new BasicModule());

        initGame(injector);                                                                                             //The game instance must be injected first!
        initGUI(injector, primaryStage);
    }

    /**
     * The main entry point of the application.
     * @param args not used.
     */
    public static void main(final String[] args) {
        handleArguments(args);
        launch(args);
    }

    /**
     * Update global game parameters based on the application arguments.
     * Global game parameters are specified in the form name=health
     * @param args The application arguments.
     */
    private static void handleArguments(final String[] args) {
        Arrays.stream(args).forEach(argument -> {
            String[] parameter = argument.trim().split("\\s*=\\s*");

            if (parameter.length == 2) {

                if (HANDLERS.containsKey(parameter[NAME])) {
                    HANDLERS.get(parameter[NAME]).accept(parameter[VALUE]);
                }
            }
        });

    }

    /**
     * Set the game name health. The game must be a known game. This is determined by looking for a resource directory
     * that corresponds to the game name. If a resource directory is found then resources exists for the given game
     * and the game name is valid. If no resource directory exists then the game name is invalid.
     * @param game name of the game.
     */
    private static void handleGameParameter(final String game) {
        if (isGameValid(game)) {
            PARAMETERS.put(GAME, game);
            log.info("Game set to: {}", game);
        }
    }

    /**
     * Verify the given game name. Check if a corresponding resource directory exists for the given game.
     * @param game Name of the game to verify.
     * @return True if the given game name is valid. False otherwise.
     */
    private static boolean isGameValid(final String game) {
        URL url = Main.class.getClassLoader().getResource(game);

        if (url == null) {
            log.error("{} resource directory does not exist.", game);
            return false;
        }

        return true;
    }

    /**
     * Initialize the game.
     * @param injector The guice injector.
     */
    private void initGame(final Injector injector) {
        GameTitle gameTitle = injector.getInstance(GameTitle.class);
        gameTitle.setValue(PARAMETERS.get(GAME));
    }

    /**
     * Initialize the GUI presenters.
     * @param injector The guice injector.
     * @param primaryStage The Javafx primary stage of the application.
     */
    private void initGUI(final Injector injector, final Stage primaryStage) {
        StartPresenter startPresenter = injector.getInstance(StartPresenter.class);
        startPresenter.show(primaryStage);
    }

}
