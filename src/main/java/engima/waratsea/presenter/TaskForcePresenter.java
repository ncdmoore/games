package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.MapException;
import engima.waratsea.model.ships.Ship;
import engima.waratsea.model.ships.ShipType;
import engima.waratsea.model.victory.VictoryException;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.view.ships.ShipViewType;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.view.FatalErrorDialog;
import engima.waratsea.view.TaskForceView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class is the presenter for the task for summary view. The task force summary gives the player an overview
 * of all task forces before the game is started.
 */
@Slf4j
@Singleton
public class TaskForcePresenter {
    private final Game game;
    private GameMap gameMap;
    private TaskForceView view;
    private Stage stage;

    private TaskForce selectedTaskForce;

    private Provider<TaskForceView> viewProvider;
    private Provider<ScenarioPresenter> scenarioPresenterProvider;
    private Provider<MainPresenter> mainPresenterProvider;
    private Provider<FatalErrorDialog> fatalErrorDialogProvider;

    /**
     * This is the constructor.
     * @param game The game object.
     * @param gameMap The preview game map.
     * @param viewProvider The corresponding view,
     * @param scenarioPresenterProvider Provides the scenario presenter.
     * @param mainPresenterProvider Provides the main presenter.
     * @param fatalErrorDialogProvider Provides the fatal error dialog.
     */
    @Inject
    public TaskForcePresenter(final Game game,
                              final GameMap gameMap,
                              final Provider<TaskForceView> viewProvider,
                              final Provider<ScenarioPresenter> scenarioPresenterProvider,
                              final Provider<MainPresenter> mainPresenterProvider,
                              final Provider<FatalErrorDialog> fatalErrorDialogProvider) {
        this.game = game;
        this.gameMap = gameMap;
        this.viewProvider = viewProvider;
        this.scenarioPresenterProvider = scenarioPresenterProvider;
        this.mainPresenterProvider = mainPresenterProvider;
        this.fatalErrorDialogProvider = fatalErrorDialogProvider;
    }

    /**
     * Creates and shows the scenario view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void show(final Stage primaryStage) {
        log.info("show.");

        view = viewProvider.get();

        this.stage = primaryStage;

        startGame();

        view.show(stage, game.getScenario());

        markTaskForces();
        markTargets();

        view.finish();

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getTaskForces().getSelectionModel().selectFirst();
    }

    /**
     * Initialize the task force data.
     */
    private void startGame() {
        try {
            game.start();
            view.setTaskForces(game.getHumanPlayer().getTaskForces());
            view.getTaskForces().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> taskForceSelected(newValue));

        } catch (ScenarioException | MapException | VictoryException ex) {
            fatalErrorDialogProvider.get().show(ex.getMessage() + ".");
        }
    }

    /**
     * Mark the task forces on the preview map.
     */
    private void markTaskForces() {
        List<TaskForce> taskForces = game.getHumanPlayer().getTaskForces();

        taskForces.forEach(taskForce -> {
            TaskForceMarkerDTO dto = new TaskForceMarkerDTO(taskForce);
            dto.setGameMap(gameMap);
            dto.setMarkerEventHandler(this::showTaskForcePopup);
            dto.setPopupEventHandler(this::closePopup);
            view.markTaskForceOnMap(dto);
        });

    }

    /**
     * Mark the task force targets on the preview map.
     */
    private void markTargets() {
        List<TaskForce> taskForces = game.getHumanPlayer().getTaskForces();
        taskForces.forEach(this::markTaskForceTargets);
    }


    /**
     * Mark the given task force's targets.
     * @param taskForce The task force whose targets are marked.
     */
    private void markTaskForceTargets(final TaskForce taskForce) {
        Optional.ofNullable(taskForce.getTargets())
                .ifPresent(targets -> targets.forEach(target -> {
                    TargetMarkerDTO dto = new TargetMarkerDTO(taskForce, target);
                    dto.setGameMap(gameMap);
                    dto.setMarkerEventHandler(this::showTargetPopup);
                    dto.setPopupEventHandler(this::closePopup);
                    view.markTargetOnMap(dto);
                }));

    }

    /**
     * Call back when a task force is selected.
     * @param taskForce the selected task force.
     */
    private void taskForceSelected(final TaskForce taskForce) {
        log.info("Task force selected: {}", taskForce.getName());

        clearAllTaskForces();
        this.selectedTaskForce = taskForce;

        getTaskForceShips();

        view.setSelectedTaskForce(selectedTaskForce);
    }

    /**
     * Clear all the task force selections.
     */
    private void clearAllTaskForces() {
        game.getHumanPlayer().getTaskForces()
                .forEach(taskForce -> view.clearTaskForce(taskForce));

    }

    /**
     *
     */
    private void getTaskForceShips() {
        // This is a map of maps. The outer key is the ship classification used by the task force summary tabs.
        // The innter map key is the actual ship type.
        Map<ShipViewType, Map<ShipType, List<Ship>>> ships = new HashMap<>();

        // Create the ship type sub maps. These are the inner maps.
        Stream.of(ShipViewType.values()).forEach(shipViewType -> ships.put(shipViewType, new HashMap<>()));

        Map<ShipType, List<Ship>> shipMap = selectedTaskForce.getShipTypeMap();

        shipMap.forEach((shipType, shipsOfThatType) -> {
            ShipViewType viewType = ShipViewType.get(shipType);
            Map<ShipType, List<Ship>> subMap = ships.get(viewType);
            subMap.put(shipType, shipsOfThatType);
        });
    }


    /**
     * Call back for the continue button.
     */
    private void continueButton() {
        log.info("continue button");
        mainPresenterProvider.get().show(stage);

    }

    /**
     * Call back for the back button.
     */
    private void backButton() {
        scenarioPresenterProvider.get().show(stage);
    }

    /**
     * Task force map grid clicked. Show the task force's corresponding popup.
     * @param event Mouse event.
     */
    private void showTaskForcePopup(final MouseEvent event) {
        Object o = event.getSource();

        List<String> names = view.getTaskForceFromMarker(o).stream().sorted().collect(Collectors.toList());

        log.info("clicked on {}", names.stream().sorted().collect(Collectors.joining(",")));

        List<TaskForce> taskForces = game.getHumanPlayer().getTaskForces();

        List<TaskForce> selected = taskForces.stream()
                .filter(taskForce -> names.contains(taskForce.getName()))
                .collect(Collectors.toList());

        int index = selectTheNextTaskForce(selected);

        // Notify view that the task force has been selected.
        // This keeps the view list in sync with the grid clicks.
        view.getTaskForces().getSelectionModel().select(selected.get(index));

        // Select the task force. This is needed for clicks that don't change the
        // task force, but redisplay the popup.
        taskForceSelected(selected.get(index));
    }

    /**
     * A task force marker has been clicked. Select the next task force that resides in the marker's grid.
     * @param selected The list of task forces residing in the marker's grid.
     * @return The index of the now selected task force.
     */
    private int selectTheNextTaskForce(final List<TaskForce> selected) {
        int index = 0;
        if (selected.size() > 1) {
            index = selected.indexOf(selectedTaskForce) + 1;
            if (index >= selected.size()) {
                index = 0;
            }
        }
        return index;
    }

    /**
     * Show the target popup upon target clicks.
     * @param event The mouse event.
     */
    private void showTargetPopup(final MouseEvent event) {
        Object o = event.getSource();
        view.selectTarget(o);
    }

    /**
     * Close the popup.
     * @param event the mouse event.
     */
    private void closePopup(final MouseEvent event) {
        view.closePopup(event);
    }

    /**
     * A fatal error has occurred.
     * @param errorText The text to display on the GUI.
     */
    private void fatalError(final String errorText) {
        fatalErrorDialogProvider.get().show(errorText + game.getScenario().getTitle() + "'.");
    }
}
