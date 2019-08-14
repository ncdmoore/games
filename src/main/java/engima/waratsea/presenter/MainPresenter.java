package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
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

    private Provider<MainView> viewProvider;
    private MainView view;
    private Stage stage;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param viewProvider The view provider.
     */
    @Inject
    public MainPresenter(final Game game,
                         final Provider<MainView> viewProvider) {
        this.game = game;
        this.viewProvider = viewProvider;
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

        view.getMainMenu().getQuit().setOnAction(e -> quit());
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
     * Quit the game.
     */
    private void quit() {
        stage.close();
    }
}
