package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.view.StartView;

/**
 * This class is the presenter for the start view or start scene of the entire application.
 * It is responsible for starting new games, loading saved games, setting game options and quiting the game.
 */
@Slf4j
@Singleton
public class StartPresenter {
    private StartView view;
    private Stage stage;

    private Provider<StartView> viewProvider;
    private Provider<ScenarioPresenter> scenarioPresenterProvider;

    /**
     * The constructor for the start presenter. Guice will inject the view and the scenario presenter.
     *
     * @param viewProvider The starting view.
     * @param scenarioPresenterProvider The scenario presenter provider. The scenario presenter is obtained from this
     *                                  provider
     */
    @Inject
    public StartPresenter(final Provider<StartView> viewProvider,
                          final Provider<ScenarioPresenter> scenarioPresenterProvider) {
        this.viewProvider = viewProvider;
        this.scenarioPresenterProvider = scenarioPresenterProvider;
    }

    /**
     * Create and show the starting view.
     *
     * @param primaryStage the primary stage.
     */
    public void show(final Stage primaryStage) {
        log.info("show.");

        view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage);

        view.getNewButton().setOnAction(event -> newGame());
        view.getSavedButton().setOnAction(event -> savedGame());
        view.getOptionsButton().setOnAction(event -> options());
        view.getQuitButton().setOnAction(event -> quitGame());
    }

    /**
     * Call back for the new game button.
     */
    private void newGame() {
        log.info("New Game.");

        scenarioPresenterProvider.get().show(stage);
    }

    /**
     * Call back for saved game button.
     */
    private void savedGame() {
        log.info("Saved Game.");
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
        log.info("Quit Game");
        stage.close();
    }
}
