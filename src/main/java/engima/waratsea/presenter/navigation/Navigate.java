package engima.waratsea.presenter.navigation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.presenter.FlotillaPresenter;
import engima.waratsea.presenter.MainPresenter;
import engima.waratsea.presenter.MinefieldPresenter;
import engima.waratsea.presenter.Presenter;
import engima.waratsea.presenter.ScenarioPresenter;
import engima.waratsea.presenter.SquadronPresenter;
import engima.waratsea.presenter.StartPresenter;
import engima.waratsea.presenter.TaskForcePresenter;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the navigation through the initial game setup screens.
 *
 */
@Singleton
public class Navigate {

    private Map<Class<?>, Class<?>> nextPage = new HashMap<>();
    private Map<Class<?>, Class<?>> prevPage = new HashMap<>();
    private Map<Class<?>, Pair<Provider<? extends Presenter>, Boolean>> presenterMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param startPresenterProvider Provides the start screen.
     * @param scenarioPresenterProvider Provides the scenario selection screen.
     * @param taskForcePresenterProvider Provides the task force summary screen.
     * @param flotillaPresenterProvider Provides the flotilla summary screen.
     * @param minefieldPresenterProvider Provides the mine field screen.
     * @param squadronPresenterProvider Provides the squadron deployment screen.
     * @param mainPresenterProvider Provies the main game screen.
     */
    @Inject
    public Navigate(final Provider<StartPresenter> startPresenterProvider,
                    final Provider<ScenarioPresenter> scenarioPresenterProvider,
                    final Provider<TaskForcePresenter> taskForcePresenterProvider,
                    final Provider<FlotillaPresenter> flotillaPresenterProvider,
                    final Provider<MinefieldPresenter> minefieldPresenterProvider,
                    final Provider<SquadronPresenter> squadronPresenterProvider,
                    final Provider<MainPresenter> mainPresenterProvider) {

        nextPage.put(StartPresenter.class, ScenarioPresenter.class);
        nextPage.put(ScenarioPresenter.class, TaskForcePresenter.class);
        nextPage.put(TaskForcePresenter.class, FlotillaPresenter.class);
        nextPage.put(FlotillaPresenter.class, MinefieldPresenter.class);
        nextPage.put(MinefieldPresenter.class, SquadronPresenter.class);
        nextPage.put(SquadronPresenter.class, MainPresenter.class);

        prevPage.put(ScenarioPresenter.class, StartPresenter.class);
        prevPage.put(TaskForcePresenter.class, ScenarioPresenter.class);
        prevPage.put(FlotillaPresenter.class, TaskForcePresenter.class);
        prevPage.put(MinefieldPresenter.class, FlotillaPresenter.class);
        prevPage.put(SquadronPresenter.class, MinefieldPresenter.class);
        //There is no back button from the main presenter.

        presenterMap.put(StartPresenter.class, new Pair<>(startPresenterProvider, true));
        presenterMap.put(ScenarioPresenter.class, new Pair<>(scenarioPresenterProvider, true));
        presenterMap.put(TaskForcePresenter.class, new Pair<>(taskForcePresenterProvider, true));
        presenterMap.put(FlotillaPresenter.class, new Pair<>(flotillaPresenterProvider, true));
        presenterMap.put(MinefieldPresenter.class, new Pair<>(minefieldPresenterProvider, true));
        presenterMap.put(SquadronPresenter.class, new Pair<>(squadronPresenterProvider, true));
        presenterMap.put(MainPresenter.class, new Pair<>(mainPresenterProvider, true));
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
            pair.getKey().get().show(stage);
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
