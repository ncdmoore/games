package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.presenter.navigation.Navigate;
import engima.waratsea.view.MinefieldView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is the presenter for the minefield placement view. The minefield placement view gives the player an the opportunity
 * to place minefields in certain "zones". The scenario will define in which zones a side may place minefields. Not all scenarios
 * allow for the placement of minefields.
 */
@Slf4j
@Singleton
public class MinefieldPresenter implements Presenter {
    private final Game game;

    private MinefieldView view;
    private Stage stage;

    private Provider<MinefieldView> viewProvider;
    private Navigate navigate;

    /**
     * The constructor called by guice.
     *
     * @param game The game object.
     * @param viewProvider Provides the minefield view.
     * @param navigate Provides screen navigation.
     */
    @Inject
    public MinefieldPresenter(final Game game,
                              final Provider<MinefieldView> viewProvider,
                              final Navigate navigate) {
        this.game = game;
        this.viewProvider = viewProvider;
        this.navigate = navigate;
    }

    /**
     * Creates and shows the minefield view.
     *
     * @param primaryStage the stage that the scenario view is placed.
     */
    public void show(final Stage primaryStage) {
        this.stage = primaryStage;

        view = viewProvider.get();

        setMinefields();

        view.show(stage, game.getScenario());


        view.getContinueButton().setOnAction(event -> continueButton());
        view.getBackButton().setOnAction(event -> backButton());

        view.getMinefields().getSelectionModel().selectFirst();
    }

    /**
     * Set the human player's minefields.
     */
    private void setMinefields() {
        view.setMinefields(game.getHumanPlayer().getMinefields());
        view.getMinefields().getSelectionModel().selectedItemProperty().addListener((v, oldValue, newValue) -> minefieldSelected(newValue));
    }

    /**
     * Select a minefield.
     *
     * @param minefield The selected minefield.
     */
    private void minefieldSelected(final Minefield minefield) {
        log.info("selected minefield {}", minefield);
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
