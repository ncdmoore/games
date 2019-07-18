package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.data.GameData;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.victory.VictoryException;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.SavedGameView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SavedGamePresenter implements Presenter {
    private Game game;
    private Stage stage;
    private GameData selectedSavedGame;

    private SavedGameView view;

    private Provider<SavedGameView> viewProvider;
    private Navigate navigate;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    /**
     * The constructor for the saved game presenter. Called by guice.
     *
     * @param game The game.
     * @param viewProvider The starting view.
     * @param navigate Used to navigate to the next screen.
     * @param fatalErrorDialogProvider provides the fatal error dialog.
     */
    @Inject
    public SavedGamePresenter(final Game game,
                              final Provider<SavedGameView> viewProvider,
                              final Navigate navigate,
                              final Provider<FatalErrorDialog> fatalErrorDialogProvider) {

        this.game = game;
        this.viewProvider = viewProvider;
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
        view = viewProvider.get();

        this.stage = primaryStage;

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
     * Callback when a saved game is selected.
     *
     * @param savedGame The selected game.
     */
    private void gameSelected(final GameData savedGame) {
        this.selectedSavedGame = savedGame;
        view.setSelectedSavedGame(savedGame);
    }

    /**
     * Call back for the new game button.
     */
    private void continueButton() {

        // get the selected scenario, side and saved game name from the game data.
        Scenario selectedScenario = selectedSavedGame.getScenario();
        Side side = selectedSavedGame.getHumanSide();
        String savedGameName = selectedSavedGame.getSavedGameName();

        game.setExisting();
        game.setScenario(selectedScenario);
        game.setHumanSide(side);
        game.setSavedGameName(savedGameName);

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
            view.getSavedGames().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> gameSelected(newValue));
            view.getSavedGames().getSelectionModel().selectFirst();

        } catch (ScenarioException ex) {
            log.error("Unable to load any of the saved game scenarios", ex);
            fatalErrorDialogProvider.get().show("Unable to load any of the saved game scenarios.");
        }

       /* if (view.getScenarios().getItems().isEmpty()) {                                                                 // Only initialize the list once.
            try {
                view.setScenarios(game.initScenarios());
                view.getScenarios().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> scenarioSelected(newValue));
                view.getScenarios().getSelectionModel().select(0);
            } catch (ScenarioException ex) {
                log.error("Unable to load scenario summaries", ex);
                fatalErrorDialogProvider.get().show("Unable to load any game scenarios.");
            }
        } else {
            view.getScenarios().getSelectionModel().select(0);                                                   // Ensure that the first scenario is always selected.
        }*/
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
