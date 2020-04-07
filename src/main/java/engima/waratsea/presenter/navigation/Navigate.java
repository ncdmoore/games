package engima.waratsea.presenter.navigation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.presenter.preview.FlotillaPresenter;
import engima.waratsea.presenter.MainPresenter;
import engima.waratsea.presenter.preview.MinefieldPresenter;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.preview.SavedGamePresenter;
import engima.waratsea.presenter.preview.scenario.ScenarioPresenter;
import engima.waratsea.presenter.preview.SquadronPresenter;
import engima.waratsea.presenter.preview.StartPresenter;
import engima.waratsea.presenter.preview.TaskForcePresenter;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the navigation through the initial game setup screens.
 *
 * There are currently two navigation paths.
 *
 * (1) New Game
 * (2) Saved Game
 *
 * Each path is completely independent of the other.
 * Each path may reuse the same presenters.
 *
 */
@Singleton
public class Navigate {

    private Map<Class<?>, Class<?>> nextPage;
    private Map<Class<?>, Class<?>> prevPage;

    private Map<Class<?>, Class<?>> nextNewGamePage = new HashMap<>();
    private Map<Class<?>, Class<?>> prevNewGamePage = new HashMap<>();

    private Map<Class<?>, Class<?>> nextSavedGamePage = new HashMap<>();
    private Map<Class<?>, Class<?>> prevSavedGamePage = new HashMap<>();

    private final Map<Class<?>, Pair<Provider<? extends Presenter>, Boolean>> presenterMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param startPresenterProvider Provides the start screen.
     * @param scenarioPresenterProvider Provides the scenario selection screen.
     * @param taskForcePresenterProvider Provides the task force summary screen.
     * @param flotillaPresenterProvider Provides the flotilla summary screen.
     * @param minefieldPresenterProvider Provides the mine field screen.
     * @param squadronPresenterProvider Provides the squadron deployment screen.
     * @param mainPresenterProvider Provides the main game screen.
     * @param savedGamePresenterProvider Provides the saved game screen.
     */
    //CHECKSTYLE:OFF
    @Inject
    public Navigate(final Provider<StartPresenter> startPresenterProvider,
                    final Provider<ScenarioPresenter> scenarioPresenterProvider,
                    final Provider<TaskForcePresenter> taskForcePresenterProvider,
                    final Provider<FlotillaPresenter> flotillaPresenterProvider,
                    final Provider<MinefieldPresenter> minefieldPresenterProvider,
                    final Provider<SquadronPresenter> squadronPresenterProvider,
                    final Provider<MainPresenter> mainPresenterProvider,
                    final Provider<SavedGamePresenter> savedGamePresenterProvider) {
        //CHECKSTYLE:ON

        setUpNewGamePath();
        setUpSavedGamePath();

        presenterMap.put(StartPresenter.class, new Pair<>(startPresenterProvider, true));
        presenterMap.put(ScenarioPresenter.class, new Pair<>(scenarioPresenterProvider, true));
        presenterMap.put(TaskForcePresenter.class, new Pair<>(taskForcePresenterProvider, true));
        presenterMap.put(FlotillaPresenter.class, new Pair<>(flotillaPresenterProvider, true));
        presenterMap.put(MinefieldPresenter.class, new Pair<>(minefieldPresenterProvider, true));
        presenterMap.put(SquadronPresenter.class, new Pair<>(squadronPresenterProvider, true));
        presenterMap.put(MainPresenter.class, new Pair<>(mainPresenterProvider, true));
        presenterMap.put(SavedGamePresenter.class, new Pair<>(savedGamePresenterProvider, true));

        //Default path is new game.
        setNewGamePath();
    }

    /**
     * Setup the new game navigation path.
     */
    public void setNewGamePath() {
        nextPage = nextNewGamePage;
        prevPage = prevNewGamePage;
    }

    /**
     * Setup the saved game navigation path.
     */
    public void setSavedGamePath() {
        nextPage = nextSavedGamePage;
        prevPage = prevSavedGamePage;
    }

    /**
     * Go to the next screen.
     *
     * @param clazz The class of the current screen's presenter.
     * @param stage The javafx stage of the next screen.
     */
    public void goNext(final Class<?> clazz, final Stage stage) {
        Class<?> page = nextPage.get(clazz);

        Pair<Provider<? extends Presenter>, Boolean> pair = presenterMap.get(page);
        if (pair.getValue()) {
            pair.getKey().get().show(stage);
        } else {
            goNext(page, stage);
        }
    }

    /**
     * Go to the previous screen.
     *
     * @param clazz The class of the current screen's presenter.
     * @param stage The javafx stage of the previous screen.
     */
    public void goPrev(final Class<?> clazz, final Stage stage) {
        Class<?> page = prevPage.get(clazz);

        Pair<Provider<? extends Presenter>, Boolean> pair = presenterMap.get(page);
        if (pair.getValue()) {
            pair.getKey().get().reShow(stage);
        } else {
            goPrev(page, stage);
        }
    }

    /**
     * If this scenario has no minefields for the human player then we can remove the minefield presenter
     * as it is not needed. There are no minefields for the human player to deploy. If this scenario does
     * have minefields for the human player then add the minefield presenter.
     *
     * We need to add and remove dynamically, since the GUI allows for a person to change scenarios.
     * A scenario with no minefields can be selected first, followed by a scenario that does contain
     * minefields. Thus, we may need to first remove and then re-add the minefield presenter.
     *
     * @param scenairo The selected scenario.
     */
    public void update(final Scenario scenairo) {
        updatePresenter(MinefieldPresenter.class, scenairo.isMinefieldForHumanSide());
        updatePresenter(SquadronPresenter.class,  scenairo.isSquadronDeploymentForHumanSide());
        updatePresenter(FlotillaPresenter.class,  scenairo.isFlotillasForHumanSide());
    }

    /**
     * Set up the new game navigation path.
     */
    private void setUpNewGamePath() {
        nextNewGamePage.put(StartPresenter.class, ScenarioPresenter.class);
        nextNewGamePage.put(ScenarioPresenter.class, TaskForcePresenter.class);
        nextNewGamePage.put(TaskForcePresenter.class, FlotillaPresenter.class);
        nextNewGamePage.put(FlotillaPresenter.class, MinefieldPresenter.class);
        nextNewGamePage.put(MinefieldPresenter.class, SquadronPresenter.class);
        nextNewGamePage.put(SquadronPresenter.class, MainPresenter.class);

        prevNewGamePage.put(ScenarioPresenter.class, StartPresenter.class);
        prevNewGamePage.put(TaskForcePresenter.class, ScenarioPresenter.class);
        prevNewGamePage.put(FlotillaPresenter.class, TaskForcePresenter.class);
        prevNewGamePage.put(MinefieldPresenter.class, FlotillaPresenter.class);
        prevNewGamePage.put(SquadronPresenter.class, MinefieldPresenter.class);
        //There is no back button from the main presenter.
    }

    /**
     * Set up the saved game navigation path.
     */
    private void setUpSavedGamePath() {
        nextSavedGamePage.put(StartPresenter.class, SavedGamePresenter.class);
        nextSavedGamePage.put(SavedGamePresenter.class, MainPresenter.class);

        prevSavedGamePage.put(SavedGamePresenter.class, StartPresenter.class);
        //There is no back button from the main presenter.
    }

    /**
     * Add or remove a presenter to the navigation maps.
     *
     * @param page The presenter to be added or removed.
     * @param show True causes the given presenter to be shown. False causes the given presenter to not be shown.
     */
    private void updatePresenter(final Class<?> page, final boolean show) {
        Pair<Provider<? extends Presenter>, Boolean> oldPair = presenterMap.get(page);
        presenterMap.put(page, new Pair<>(oldPair.getKey(), show));
    }
}
