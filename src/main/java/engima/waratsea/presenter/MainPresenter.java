package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.MainView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the main presenter for the game.
 */
@Slf4j
@Singleton
public class MainPresenter implements Presenter {

    private Game game;

    private MainMapPresenter mainMapPresenter;

    private Provider<MainView> viewProvider;
    private Provider<MainMenu> menuProvider;
    private MainView view;
    private Stage stage;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param mainMapPresenter The main map presenter.
     * @param viewProvider The main view provider.
     * @param menuProvider The main menu provider.
     */
    @Inject
    public MainPresenter(final Game game,
                         final MainMapPresenter mainMapPresenter,
                         final Provider<MainView> viewProvider,
                         final Provider<MainMenu> menuProvider) {
        this.game = game;
        this.mainMapPresenter = mainMapPresenter;
        this.viewProvider = viewProvider;
        this.menuProvider = menuProvider;
    }

    /**
     * Show the main game view.
     *
     * @param primaryStage The primary Javafx stage.
     */
    @Override
    public void show(final Stage primaryStage) {

        game.save();

        view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage);

        menuProvider.get().getSave().setOnAction(event -> save());
        menuProvider.get().getQuit().setOnAction(event -> quit());

        menuProvider.get().getSquadrons().setOnAction(event -> squadrons());

        mainMapPresenter.setBaseClickHandler();
    }

    /**
     * Re show the main game view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Save the current game.
     */
    private void save() {
        game.save();
    }

    /**
     * Save the given game.
     *
     * @param savedGameName The name of the game that is saved.
     */
    private void saveAs(final String savedGameName) {
        game.save(savedGameName);
    }

    /**
     * Quit the game.
     */
    private void quit() {
        stage.close();
    }

    /**
     * Show the player's squadrons.
     */
    private void squadrons() {
        System.out.println("show squadrons");
    }
}
