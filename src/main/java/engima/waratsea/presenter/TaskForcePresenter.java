package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.Grid;
import engima.waratsea.model.ships.TaskForceState;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.Marker;
import engima.waratsea.view.map.PopUp;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.ships.TaskForce;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.TaskForceView;

import java.util.List;
import java.util.stream.Collectors;


/**
 * This class is the presenter for the task for summary view. The task force summary gives the player an overview
 * of all task forces before the game is started.
 */
@Slf4j
public class TaskForcePresenter {
    private TaskForceView view;
    private final Game game;
    private GameMap gameMap;
    private ViewProps props;
    private Stage stage;

    private TaskForce selectedTaskForce;


    private Provider<ScenarioPresenter> scenarioPresenterProvider;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    /**
     * This is the constructor.
     * @param view The corresponding view,
     * @param game The game object.
     * @param gameMap The preview game map.
     * @param props The view properties.
     * @param scenarioPresenterProvider Provides the scenario presenter.
     * @param fatalErrorDialogProvider Provides the fatal error dialog.
     */
    @Inject
    public TaskForcePresenter(final TaskForceView view,
                              final Game game,
                              final GameMap gameMap,
                              final ViewProps props,
                              final Provider<ScenarioPresenter> scenarioPresenterProvider,
                              final Provider<FatalErrorDialog> fatalErrorDialogProvider) {
        this.view = view;
        this.game = game;
        this.gameMap = gameMap;
        this.props = props;
        this.scenarioPresenterProvider = scenarioPresenterProvider;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;

        this.gameMap.init(props.getInt("taskforce.previewMap.gridSize"));
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

        markTaskForces();

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getTaskForces().getSelectionModel().select(0);
    }

    /**
     * Initialize the task force data.
     */
    private void initTaskForce() {

        try {
            game.start();
            view.setTaskForces(game.getHumanPlayer().getTaskForces());
            view.getTaskForces().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> taskForceSelected(newValue));

        } catch (ScenarioException ex) {
            fatalErrorDialogProvider.get().show("Unable to load  game scenario: '" + game.getScenario().getTitle() + "' task forces.");
        }
    }


    /**
     * Mark the task forces on the preview map.
     */
    private void markTaskForces() {
        List<TaskForce> taskForces = game.getHumanPlayer().getTaskForces();

        taskForces.forEach(taskForce -> {
            Grid grid = gameMap.getGrid(taskForce.getLocation());
            boolean active = taskForce.getState() == TaskForceState.ACTIVE;
            Marker marker = new Marker(taskForce.getName(), taskForce.getLocation(), grid.getX(), grid.getY(), grid.getSize(), active, this::mouseClick);
            PopUp popUp = new PopUp(marker, props.getInt("taskforce.previewMap.popup.xOffset"), this::closePopup);
            view.markTaskForceOnMap(marker);
            view.addTaskForcePopUp(popUp);
        });

        view.finish();
    }

    /**
     * Call back when a task force is selected.
     * @param taskForce the selected task force.
     */
    private void taskForceSelected(final TaskForce taskForce) {
        log.info("Task force selected: {}", taskForce.getName());

        game.getHumanPlayer().getTaskForces().forEach(t -> view.clearTaskForce(t));

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

    /**
     * Task force map grid clicked.
     * @param event Mouse event.
     */
    private void mouseClick(final MouseEvent event) {

        Object o = event.getSource();

        List<String> names = view.getTaskForceFromMarker(o).stream().sorted().collect(Collectors.toList());

        log.info("clicked on {}", names.stream().sorted().collect(Collectors.joining(",")));

        List<TaskForce> taskForces = game.getHumanPlayer().getTaskForces();

        List<TaskForce> selected = taskForces.stream().filter(t -> t.getName().equalsIgnoreCase(names.get(0))).collect(Collectors.toList());

        // Notify view that the task force has been selected.
        // This keeps the view list in sync with the grid clicks.
        view.getTaskForces().getSelectionModel().select(selected.get(0));

        // Select the task force. This is needed for clicks that don't change the
        // task force, but redisplay the popup.
        taskForceSelected(selected.get(0));
    }

    /**
     * Close the popup.
     *
     * @param event the mouse event.
     */
    private void closePopup(final MouseEvent event) {
        view.closePopup(event);
    }
}
