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
import engima.waratsea.presenter.StartPresenter;
import engima.waratsea.presenter.TaskForcePresenter;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the navigation through the initial game setup screens.
 *
 */
@Singleton
public class Navigate {
    private Map<Class<?>, Provider<? extends Presenter>> nextMap = new HashMap<>();
    private Map<Class<?>, Provider<? extends Presenter>> prevMap = new HashMap<>();

    private Provider<MinefieldPresenter> minefieldPresenterProvider;
    private Provider<FlotillaPresenter> flotillaPresenterProvider;

    private Provider<MainPresenter> mainPresenterProvider;

    /**
     * The constructor called by guice.
     *
     * @param startPresenterProvider Provides the start screen.
     * @param scenarioPresenterProvider Provides the scenario selection screen.
     * @param taskForcePresenterProvider Provides the task force summary screen.
     * @param flotillaPresenterProvider Provides the flotilla summary screen.
     * @param minefieldPresenterProvider Provides the mine field screen.
     * @param mainPresenterProvider Provies the main game screen.
     */
    @Inject
    public Navigate(final Provider<StartPresenter> startPresenterProvider,
                    final Provider<ScenarioPresenter> scenarioPresenterProvider,
                    final Provider<TaskForcePresenter> taskForcePresenterProvider,
                    final Provider<FlotillaPresenter> flotillaPresenterProvider,
                    final Provider<MinefieldPresenter> minefieldPresenterProvider,
                    final Provider<MainPresenter> mainPresenterProvider) {

        nextMap.put(StartPresenter.class, scenarioPresenterProvider);
        nextMap.put(ScenarioPresenter.class, taskForcePresenterProvider);
        nextMap.put(TaskForcePresenter.class, flotillaPresenterProvider);
        nextMap.put(FlotillaPresenter.class, minefieldPresenterProvider);
        nextMap.put(MinefieldPresenter.class, mainPresenterProvider);

        prevMap.put(ScenarioPresenter.class, startPresenterProvider);
        prevMap.put(TaskForcePresenter.class, scenarioPresenterProvider);
        prevMap.put(FlotillaPresenter.class, taskForcePresenterProvider);
        prevMap.put(MinefieldPresenter.class, flotillaPresenterProvider);

        //There is no back button from the main presenter.

        this.flotillaPresenterProvider = flotillaPresenterProvider;
        this.minefieldPresenterProvider = minefieldPresenterProvider;
        this.mainPresenterProvider = mainPresenterProvider;
    }

    /**
     * Go to the next screen.
     *
     * @param clazz The class of the current screen's presenter.
     * @param stage The javafx stage of the next screen.
     */
    public void goNext(final Class<?> clazz, final Stage stage) {
        nextMap.get(clazz).get().show(stage);
    }

    /**
     * Go to the previous screen.
     *
     * @param clazz The class of the current screen's presenter.
     * @param stage The javafx stage of the previous screen.
     */
    public void goPrev(final Class<?> clazz, final Stage stage) {
        prevMap.get(clazz).get().show(stage);
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

        if (scenairo.isMinefieldForHumanSide()) {
            addMinefieldPresenter();
        } else {
            removeMinefieldPresenter();
        }
    }

    /**
     * Add the minefield presenter to the navigation maps.
     */
    private void addMinefieldPresenter() {
        nextMap.put(FlotillaPresenter.class, minefieldPresenterProvider);
        nextMap.put(MinefieldPresenter.class, mainPresenterProvider);
        prevMap.put(MinefieldPresenter.class, flotillaPresenterProvider);

    }

    /**
     * Remove the minefield presenter from the navigation maps.
     */
    private void removeMinefieldPresenter() {
        nextMap.put(FlotillaPresenter.class, nextMap.get(MinefieldPresenter.class));
    }
}
