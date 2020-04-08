package engima.waratsea.presenter.preview;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Resource;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.victory.VictoryException;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.scenario.ScenarioViewModel;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.preview.ScenarioView;

/**
 * This is the scenario presenter. It is responsible for selecting a game side and scenario.
 */
@Slf4j
@Singleton
public class ScenarioPresenter implements Presenter {
    private ScenarioView view;
    private Stage stage;

    private Scenario selectedScenario;
    private Provider<ScenarioView> viewProvider;
    private Navigate navigate;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    private ScenarioViewModel scenarioViewModel;

    private Game game;

    /**
     * The constructor for the scenario presenter. Guice will inject the view.
     *
     * @param game The game.
     * @param viewProvider Scenario view.
     * @param navigate Controls the screen navigation.
     * @param fatalErrorDialogProvider provides the fatal error dialog.
     * @param scenarioViewModel The scenario view model.
     */
    @Inject
    public ScenarioPresenter(final Game game,
                             final Provider<ScenarioView> viewProvider,
                             final Navigate navigate,
                             final Provider<FatalErrorDialog> fatalErrorDialogProvider,
                             final ScenarioViewModel scenarioViewModel) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.navigate = navigate;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;

        this.scenarioViewModel = scenarioViewModel;
    }

    /**
     * Creates and shows the scenario view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    @Override
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        view = viewProvider
                .get()
                .bind(scenarioViewModel);

        initScenarios();
        selectFirstScenario();

        view.show(stage);
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
    }

    /**
     * Re show the scenario view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Callback when a scenario is selected.
     *
     * @param scenario The selected scenario.
     */
    private void scenarioSelected(final Scenario scenario) {
        selectedScenario = scenario;
        scenarioViewModel.setModel(scenario);
    }

    /**
     * Callback when the continue button is clicked.
     *
     * The scenario and side have now been selected and are set in the game object.
     */
    private void continueButton() {
        Side side = view.getRadioButtonAllies().isSelected() ? Side.ALLIES : Side.AXIS;

        game.setNew();
        game.setScenario(selectedScenario);
        game.setHumanSide(side);
        game.setSavedGameName(Resource.DEFAULT_SAVED_GAME);

        startGame();

        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Callback when the back button is clicked. Return to the start screen.
     */
    private void backButton() {
        navigate.goPrev(this.getClass(), stage);
    }

    /**
     * Load the scenario list with the defined scenarios.
     */
    private void initScenarios() {
        try {
            view.setScenarios(game.initScenarios());
            view.getScenarios()
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((v, oldValue, newValue) -> scenarioSelected(newValue));
        } catch (ScenarioException ex) {
            log.error("Unable to load scenario summaries", ex);
            fatalErrorDialogProvider.get().show("Unable to load any game scenarios.");
        }
    }

    /**
     * Select the first scenario.
     */
    private void selectFirstScenario() {
        view
                .getScenarios()
                .getSelectionModel()
                .selectFirst();
    }

    /**
     * Initialize the task force data.
     */
    private void startGame() {
        try {
            game.startNew();
        } catch (ScenarioException | MapException | VictoryException | SquadronException ex) {
            fatalErrorDialogProvider.get().show(ex.getMessage() + ".");
        }
    }
}
