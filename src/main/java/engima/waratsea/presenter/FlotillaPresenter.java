package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.game.Game;
import engima.waratsea.presenter.dto.map.TaskForceMarkerDTO;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.FlotillaView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * This class is the presenter for the flotilla summary view. The flotilla summary gives the player an overview
 * of all flotillas before the game is started.
 */
@Slf4j
@Singleton
public class FlotillaPresenter implements Presenter {
    private Provider<FlotillaView> viewProvider;

    private final Game game;
    private FlotillaView view;
    private Stage stage;

    private Navigate navigate;

    private Flotilla selectedFlotilla;

    /**
     * This is the constructor.
     *
     * @param game The game object.
     * @param viewProvider The corresponding view,
     * @param navigate Provides screen navigation.
     */
    @Inject
    public FlotillaPresenter(final Game game,
                             final Provider<FlotillaView> viewProvider,
                             final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.navigate = navigate;
    }

    /**
     * Show the primary stage.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void show(final Stage primaryStage) {
        this.view = viewProvider.get();

        setFlotillas();

        this.stage = primaryStage;

        view.show(stage, game.getScenario());

        markFlotillas();

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getFlotillas().getSelectionModel().selectFirst();
    }

    /**
     * Set the human player's flotillas.
     */
    private void setFlotillas() {
        view.setFlotillas(game.getHumanPlayer().getFlotillas());
        view.getFlotillas().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> flotillaSelected(newValue));
    }

    /**
     * Mark the flotillas on the preview map.
     */
    private void markFlotillas() {
        game
                .getHumanPlayer()
                .getFlotillas()
                .forEach(this::markFlotilla);
    }

    /**
     * Mark the given flotilla on the preview map.
     *
     * @param flotilla The selected flotilla.
     */
    private void markFlotilla(final Flotilla flotilla) {
        TaskForceMarkerDTO dto = new TaskForceMarkerDTO(flotilla);
        dto.setMarkerEventHandler(this::showPopup);
        dto.setPopupEventHandler(this::closePopup);
        view.markFlotillaOnMap(dto);
    }

    /**
     * Select a flotilla.
     *
     * @param flotilla The selected flotilla.
     */
    private void flotillaSelected(final Flotilla flotilla) {
        clearAllFlotillas();

        selectedFlotilla = flotilla;

        view.setSelectedFlotilla(flotilla);
    }

    /**
     * Clear all the task force selections.
     */
    private void clearAllFlotillas() {
        game.getHumanPlayer().getFlotillas()
                .forEach(taskForce -> view.clearFlotilla(taskForce));
    }

    /**
     * Show the flotilla's popup.
     *
     * @param event The mouse event click on the flotilla marker.
     */
    @SuppressWarnings("unchecked")
    private void showPopup(final MouseEvent event) {
        Shape shape = (Shape) event.getSource();
        List<Flotilla> selected = (List<Flotilla>) shape.getUserData();

        Flotilla flotilla = selected.get(0);

        // Notify view that the flotilla has been selected.
        // This keeps the view list in sync with the grid clicks.
        view.getFlotillas().getSelectionModel().select(flotilla);

        // Select the flotilla. This is needed for clicks that don't change the
        // flotilla, but redisplay the popup.
        flotillaSelected(flotilla);
    }

    /**
     * Hide the flotilla's popup.
     *
     * @param event The mouse event click on the popup.
     */
    private void closePopup(final MouseEvent event) {
        view.closePopup(event);
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
}
