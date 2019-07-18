package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.ship.Ship;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.dto.map.TargetMarkerDTO;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.presenter.ship.ShipDetailsDialog;
import engima.waratsea.view.TaskForceView;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * This class is the presenter for the task force summary view. The task force summary gives the player an overview
 * of all task forces before the game is started.
 */
@Slf4j
@Singleton
public class TaskForcePresenter implements Presenter {
    private final Game game;
    private TaskForceView view;
    private Stage stage;

    private TaskForce selectedTaskForce;

    private Provider<TaskForceView> viewProvider;
    private Provider<ShipDetailsDialog> shipDetailsDialogProvider;
    private Navigate navigate;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param viewProvider The corresponding view,
     * @param shipDetailsDialogProvider The ship details dialog provider.
     * @param navigate Provides screen navigation.
     */
    @Inject
    public TaskForcePresenter(final Game game,
                              final Provider<TaskForceView> viewProvider,
                              final Provider<ShipDetailsDialog> shipDetailsDialogProvider,
                              final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.shipDetailsDialogProvider = shipDetailsDialogProvider;
        this.navigate = navigate;
    }

    /**
     * Creates and shows the task force view.
     *
     * @param primaryStage the stage that the task force view is placed.
     */
    @Override
    public void show(final Stage primaryStage) {
        setupView(primaryStage);
    }

    /**
     * Reshows the task force view.
     *
     * @param primaryStage the stage that the task force view is placed.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        setupView(primaryStage);
    }

    /**
     * Setup the task force view.
     *
     * @param primaryStage The primary stage.
     */
    private void setupView(final Stage primaryStage) {
        view = viewProvider.get();

        this.stage = primaryStage;

        view.setTaskForces(game.getHumanPlayer().getTaskForces());
        view.getTaskForces().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> taskForceSelected(newValue));

        navigate.update(game.getScenario());

        view.show(stage, game.getScenario());

        markTaskForces();
        markTargets();

        view.finish();
        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());
        view.getTaskForces().getSelectionModel().selectFirst();
    }

    /**
     * Mark the task forces on the preview map.
     */
    private void markTaskForces() {
        game
                .getHumanPlayer()
                .getTaskForces()
                .forEach(this::markTaskForce);
    }

    /**
     * Mark the given task force on the preveiw map.
     *
     * @param taskForce The selected task force.
     */
    private void markTaskForce(final TaskForce taskForce) {
        TaskForceMarkerDTO dto = new TaskForceMarkerDTO(taskForce);
        dto.setMarkerEventHandler(this::showTaskForcePopup);
        dto.setPopupEventHandler(this::closePopup);
        view.markTaskForceOnMap(dto);
    }

    /**
     * Mark the task force targets on the preview map.
     */
    private void markTargets() {
        game
                .getHumanPlayer()
                .getTaskForces()
                .forEach(this::markTaskForceTargets);
    }

    /**
     * Mark the given task force's targets.
     *
     * @param taskForce The task force whose targets are marked.
     */
    private void markTaskForceTargets(final TaskForce taskForce) {
        Optional.ofNullable(taskForce.getTargets())
                .orElseGet(Collections::emptyList)
                .forEach(target -> markTaskForceTarget(taskForce, target));
    }

    /**
     * Mark the given task force's given target.
     *
     * @param taskForce The selected task force.
     * @param target One of the task force's targets.
     */
    private void markTaskForceTarget(final TaskForce taskForce, final Target target) {
        TargetMarkerDTO dto = new TargetMarkerDTO(taskForce, target);
        dto.setMarkerEventHandler(this::showTargetPopup);
        dto.setPopupEventHandler(this::closePopup);
        view.markTargetOnMap(dto);
    }

    /**
     * Call back when a task force is selected.
     *
     * @param taskForce the selected task force.
     */
    private void taskForceSelected(final TaskForce taskForce) {
        log.info("Task force selected: {}", taskForce.getName());

        clearAllTaskForces();
        this.selectedTaskForce = taskForce;

        view.setSelectedTaskForce(selectedTaskForce);
        view.getShipButtons()
                .forEach(button -> button.setOnAction(this::displayShipDialog));
    }

    /**
     * Clear all the task force selections.
     */
    private void clearAllTaskForces() {
        game.getHumanPlayer().getTaskForces()
                .forEach(taskForce -> view.clearTaskForce(taskForce));
    }

    /**
     * Call back for the continue button.
     */
    private void continueButton() {
        navigate.goNext(this.getClass(), stage);
    }

    /**
     * Call back for the back button.
     */
    private void backButton() {
        navigate.goPrev(this.getClass(), stage);
    }

    /**
     * Task force map grid clicked. Show the task force's corresponding popup.
     *
     * @param event Mouse event.
     */
    @SuppressWarnings("unchecked")
    private void showTaskForcePopup(final MouseEvent event) {
        Shape shape = (Shape) event.getSource();
        List<TaskForce> selected = (List<TaskForce>) shape.getUserData();

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
     *
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
     *
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
     * Display the ship details dialog.
     *
     * @param event The mouse click event.
     */
    private void displayShipDialog(final ActionEvent event) {
        Button button = (Button) event.getSource();
        Ship ship = (Ship) button.getUserData();
        shipDetailsDialogProvider.get().show(ship);
    }
}
