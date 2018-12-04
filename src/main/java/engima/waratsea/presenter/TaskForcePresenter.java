package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.TaskForceView;

/**
 * This class is the presenter for the task for summary view. The task force summary gives the player an overview
 * of all task forces before the game is started.
 */
@Slf4j
public class TaskForcePresenter {
    private TaskForceView view;
    private final Game game;
    private Stage stage;


    private TaskForce selectedTaskForce;

    private Provider<ScenarioPresenter> scenarioPresenterProvider;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    /**
     * This is the constructor.
     * @param view The corresponding view,
     * @param game The game object.
     * @param scenarioPresenterProvider Provides the scenario presenter.
     * @param fatalErrorDialogProvider Provides the fatal error dialog.
     */
    @Inject
    public TaskForcePresenter(final TaskForceView view,
                              final Game game,
                              final Provider<ScenarioPresenter> scenarioPresenterProvider,
                              final Provider<FatalErrorDialog> fatalErrorDialogProvider) {
        this.view = view;
        this.game = game;
        this.scenarioPresenterProvider = scenarioPresenterProvider;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;
    }

    /**
     * Creates and shows the scenario view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void init(final Stage primaryStage) {
        log.info("init.");

        this.stage = primaryStage;

        initTaskForce();

        view.show(stage);
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

    }

    /**
     * Initialize the task force data.
     */
    private void initTaskForce() {

        try {
            game.initTaskForces();
            view.setTaskForces(game.getHumanPlayer().getTaskForces());
            view.getTaskForces().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> taskForceSelected(newValue));
            view.getTaskForces().getSelectionModel().select(0);

        } catch (ScenarioException ex) {
            fatalErrorDialogProvider.get().show("Unable to load  game scenario: '" + game.getScenario().getTitle() + "' task forces.");
        }
    }

    /**
     * Call back when a task force is selected.
     * @param taskForce the selected task force.
     */
    private void taskForceSelected(final TaskForce taskForce) {
        log.info("Task force selected: {}", taskForce.getName());
        this.selectedTaskForce = taskForce;
        view.setTaskForce(selectedTaskForce);
    }

    /**
     * Call back for the continue button.
     */
    private void continueButton() {
        log.info("continue button");
    }

    /**
     * Call back for the back button.
     */
    private void backButton() {
        scenarioPresenterProvider.get().init(stage);
    }
}
