package engima.waratsea.presenter.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.victory.VictoryException;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.preview.SavedGameView;
import engima.waratsea.viewmodel.SavedGameViewModel;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SavedGamePresenter implements Presenter {
    private final Game game;
    private Stage stage;

    private SavedGameView view;

    private final Provider<SavedGameView> viewProvider;
    private final Navigate navigate;
    private final Provider<FatalErrorDialog> fatalErrorDialogProvider;

    private final SavedGameViewModel scenarioViewModel;

    /**
     * The constructor for the saved game presenter. Called by guice.
     *
     * @param game The game.
     * @param viewProvider The starting view.
     * @param scenarioViewModel The scenario view model.
     * @param navigate Used to navigate to the next screen.
     * @param fatalErrorDialogProvider provides the fatal error dialog.
     */
    @Inject
    public SavedGamePresenter(final Game game,
                              final Provider<SavedGameView> viewProvider,
                              final SavedGameViewModel scenarioViewModel,
                              final Navigate navigate,
                              final Provider<FatalErrorDialog> fatalErrorDialogProvider) {

        this.game = game;
        this.viewProvider = viewProvider;
        this.scenarioViewModel = scenarioViewModel;
        this.navigate = navigate;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;
    }

    /**
     * Create and show the starting view.
     *
     * @param primaryStage the primary stage.
     */
    @Override
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        view = viewProvider
                .get()
                .bind(scenarioViewModel);

        initScenarios();

        view.show(stage);
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
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
    private void continueButton() {
        game.init(scenarioViewModel.getGame().getValue());
        startGame();
        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Call back for saved game button.
     */
    private void backButton() {
        navigate.goPrev(this.getClass(), stage);
    }

    /**
     * Load the scenario list with the defined scenarios.
     */
    private void initScenarios() {
        try {
            view.setSavedGames(game.initGames());
            view.getSavedGames().getSelectionModel().selectFirst();
        } catch (ScenarioException ex) {
            log.error("Unable to load any of the saved game scenarios", ex);
            fatalErrorDialogProvider.get().show("Unable to load any of the saved game scenarios.");
        }
    }

    /**
     * Initialize the task force data.
     */
    private void startGame() {
        try {
            game.startExisting();
        } catch (ScenarioException | MapException | VictoryException  ex) {
            fatalErrorDialogProvider.get().show(ex.getMessage() + ".");
        }
    }
}
