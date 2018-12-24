package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.ScenarioView;

/**
 * This is the scenario presenter. It is responsible for selecting a game side and scenario.
 */
@Slf4j
@Singleton
public class ScenarioPresenter {
    private ScenarioView view;
    private Stage stage;

    private Scenario selectedScenario;
    private Provider<ScenarioView> viewProvider;
    private Provider<StartPresenter> startPresenterProvider;
    private Provider<TaskForcePresenter> taskForcePresenterProvider;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    private Game game;

    /**
     * The constructor for the scenario presenter. Guice will inject the view.
     * @param game The game.
     * @param viewProvider Scenario view.
     * @param startPresenterProvider provides the start presenter.
     * @param taskForcePresenterProvider provides the task force presenter.
     * @param fatalErrorDialogProvider provides the fatal error dialog.
     */
    @Inject
    public ScenarioPresenter(final Game game,
                             final Provider<ScenarioView> viewProvider,
                             final Provider<StartPresenter> startPresenterProvider,
                             final Provider<TaskForcePresenter> taskForcePresenterProvider,
                             final Provider<FatalErrorDialog> fatalErrorDialogProvider) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.startPresenterProvider = startPresenterProvider;
        this.taskForcePresenterProvider = taskForcePresenterProvider;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;
    }

    /**
     * Creates and shows the scenario view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void init(final Stage primaryStage) {
        log.info("init.");

        view = viewProvider.get();

        this.stage = primaryStage;

        initScenarios();

        view.show(stage);
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

    }

    /**
     * Callback when a scenario is selected.
     *
     * @param scenario The selected scenario.
     */
    private void scenarioSelected(final Scenario scenario) {
        log.info("Scenario selected: {}", scenario.getTitle());
        this.selectedScenario = scenario;
        view.setScenario(selectedScenario);
    }

    /**
     * Callback when the continue button is clicked.
     *
     * The scenario and side have now been selected and are set in the game object.
     */
    private void continueButton() {
        log.info("Selected scenario {}", selectedScenario.getTitle());

        Side side = view.getRadioButtonAllies().isSelected() ? Side.ALLIES : Side.AXIS;

        log.info("Selected side {}", side);

        game.setScenario(selectedScenario);
        game.setHumanSide(side);

        taskForcePresenterProvider.get().init(stage);
    }

    /**
     * Callback when the back button is clicked. Return to the start screen.
     */
    private void backButton() {
        startPresenterProvider.get().init(stage);
    }

    /**
     * Load the scenario list with the defined scenarios.
     */
    private void initScenarios() {

        if (view.getScenarios().getItems().isEmpty()) {                                                                 // Only initialize the list once.
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
        }
    }

}
