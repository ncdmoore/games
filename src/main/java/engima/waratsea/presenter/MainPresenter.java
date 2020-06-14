package engima.waratsea.presenter;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.presenter.preview.StartPresenter;
import engima.waratsea.presenter.squadron.SquadronsDialog;
import engima.waratsea.view.MainMenu;
import engima.waratsea.view.MainView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the main presenter for the game.
 */
@Slf4j
@Singleton
public class MainPresenter implements Presenter {

    private Game game;

    private MainMapPresenter mainMapPresenter;

    private Provider<MainView> viewProvider;
    private Provider<MainMenu> menuProvider;
    private Provider<SquadronsDialog> squadronsDialogProvider;
    private Provider<StartPresenter> startPresenterProvider;
    private Stage stage;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param mainMapPresenter The main map presenter.
     * @param squadronsDialogProvider The squadrons dialog provider.
     * @param viewProvider The main view provider.
     * @param menuProvider The main menu provider.
     * @param startPresenterProvider The start screen provider.
     */
    @Inject
    public MainPresenter(final Game game,
                         final MainMapPresenter mainMapPresenter,
                         final Provider<SquadronsDialog> squadronsDialogProvider,
                         final Provider<MainView> viewProvider,
                         final Provider<MainMenu> menuProvider,
                         final Provider<StartPresenter> startPresenterProvider) {
        this.game = game;
        this.mainMapPresenter = mainMapPresenter;
        this.viewProvider = viewProvider;
        this.menuProvider = menuProvider;
        this.squadronsDialogProvider = squadronsDialogProvider;
        this.startPresenterProvider = startPresenterProvider;
    }

    /**
     * Show the main game view.
     *
     * @param primaryStage The primary Javafx stage.
     */
    @Override
    public void show(final Stage primaryStage) {

        game.save();

        MainView view = viewProvider.get();

        this.stage = primaryStage;

        view.show(stage);

        menuProvider.get().getSave().setOnAction(event -> save());
        menuProvider.get().getExitMain().setOnAction(event -> exitMain());
        menuProvider.get().getExitGame().setOnAction(event -> exitGame());

        menuProvider.get().getAirfieldSquadrons().setOnAction(event -> airfieldSquadrons());
        menuProvider.get().getTaskForceSquadrons().setOnAction(event -> taskForceSquadrons());

        mainMapPresenter.setClickHandlers();
    }

    /**
     * Re show the main game view.
     *
     * @param primaryStage The primary javafx stage.
     */
    @Override
    public void reShow(final Stage primaryStage) {
        show(primaryStage);
    }

    /**
     * Save the current game.
     */
    private void save() {
        game.save();
    }

    /**
     * Save the given game.
     *
     * @param savedGameName The name of the game that is saved.
     */
    private void saveAs(final String savedGameName) {
        game.save(savedGameName);
    }

    /**
     * Quit the current game and return to the main menu: the startup screen.
     */
    private void exitMain() {
        System.out.println("exit main");
        startPresenterProvider.get().show(stage);
    }

    /**
     * Quit the game.
     */
    private void exitGame() {
        stage.close();
    }

    /**
     * Show the player's airfield squadrons.
     */
    private void airfieldSquadrons() {
        squadronsDialogProvider.get().show(SquadronLocationType.LAND);
    }

    /**
     * Show the player's task force squadrons.
     */
    private void taskForceSquadrons() {
        squadronsDialogProvider.get().show(SquadronLocationType.SEA);
    }
}
