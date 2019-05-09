package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.view.MinefieldView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is the presenter for the minefield placement view. The minefield placement view gives the player an the opportunity
 * to place minefields in certain "zones". The scenario will define in which zones a side may place minefields. Not all scenarios
 * allow for the placement of minefields.
 */
@Slf4j
@Singleton
public class MinefieldPresenter {
    private final Game game;

    private MinefieldView view;
    private Stage stage;

    private Provider<MinefieldView> viewProvider;
    private Provider<TaskForcePresenter> taskForcePresenterProvider;
    private Provider<MainPresenter> mainPresenterProvider;

    /**
     * The constructor called by guice.
     *
     * @param game The game object.
     * @param viewProvider Provides the minefield view.
     * @param taskForcePresenterProvider Provides the task force view presenter.
     * @param mainPresenterProvider Provides the main presenter.
     */
    @Inject
    public MinefieldPresenter(final Game game,
                              final Provider<MinefieldView> viewProvider,
                              final Provider<TaskForcePresenter> taskForcePresenterProvider,
                              final Provider<MainPresenter> mainPresenterProvider) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.taskForcePresenterProvider = taskForcePresenterProvider;
        this.mainPresenterProvider = mainPresenterProvider;
    }

    /**
     * Creates and shows the minefield view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        view = viewProvider.get();

        view.show(stage, game.getScenario());


        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
    }

    /**
     * Call back for the continue button.
     */
    private void continueButton() {
        mainPresenterProvider.get().show(stage);
    }

    /**
     * Call back for the back button.
     */
    private void backButton() {
        taskForcePresenterProvider.get().show(stage);
    }
}
