package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.game.Game;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.FlotillaView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

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

        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getFlotillas().getSelectionModel().selectFirst();
    }

    /**
     * Set the human player's minefields.
     */
    private void setFlotillas() {
        view.setFlotillas(game.getHumanPlayer().getFlotillas());
        view.getFlotillas().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> selectFlotilla(newValue));
    }

    /**
     * Select a flotilla.
     *
     * @param flotilla The selected flotilla.
     */
    private void selectFlotilla(final Flotilla flotilla) {
        selectedFlotilla = flotilla;
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
