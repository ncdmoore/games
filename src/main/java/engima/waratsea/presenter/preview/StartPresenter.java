package engima.waratsea.presenter.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.navigation.Navigate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.view.preview.StartView;

/**
 * This class is the presenter for the start view or start scene of the entire application.
 * It is responsible for starting new games, loading saved games, setting game options and quiting the game.
 */
@Slf4j
@Singleton
public class StartPresenter implements Presenter {
    private Stage stage;

    private Provider<StartView> viewProvider;

    private Navigate navigate;

    /**
     * The constructor for the start presenter. Guice will inject the view and the scenario presenter.
     *
     * @param viewProvider The starting view.
     * @param navigate Used to navigate to the next screen.
     */
    @Inject
    public StartPresenter(final Provider<StartView> viewProvider,
                          final Navigate navigate) {
        this.viewProvider = viewProvider;
        this.navigate = navigate;
    }

    /**
     * Create and show the starting view.
     *
     * @param primaryStage the primary stage.
     */
    @Override
    public void show(final Stage primaryStage) {
        StartView view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage);

        view.getNewButton().setOnAction(event -> newGame());
        view.getSavedButton().setOnAction(event -> savedGame());
        view.getOptionsButton().setOnAction(event -> options());
        view.getQuitButton().setOnAction(event -> quitGame());
    }

    /**
     * Re show the starting view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Call back for the new game button.
     */
    private void newGame() {
        navigate.setNewGamePath();
        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Call back for saved game button.
     */
    private void savedGame() {
        navigate.setSavedGamePath();
        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Call back for options button.
     */
    private void options() {
        log.info("Options");
    }

    /**
     * Call back for quit game button.
     */
    private void quitGame() {
        stage.close();
    }
}
