package engima.waratsea.presenter.navigation;

import com.google.inject.Inject;
import com.google.inject.Provider;
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
public class Navigate {
    private Map<Class<?>, Provider<? extends Presenter>> nextMap = new HashMap<>();
    private Map<Class<?>, Provider<? extends Presenter>> prevMap = new HashMap<>();

    /**
     * The constructor called by guice.
     *
     * @param startPresenterProvider Provides the start screen.
     * @param scenarioPresenterProvider Provides the scenario selection screen.
     * @param taskForcePresenterProvider Provides the task force summary screen.
     * @param minefieldPresenterProvider Provides the mine field screen.
     * @param mainPresenterProvider Provies the main game screen.
     */
    @Inject
    public Navigate(final Provider<StartPresenter> startPresenterProvider,
                    final Provider<ScenarioPresenter> scenarioPresenterProvider,
                    final Provider<TaskForcePresenter> taskForcePresenterProvider,
                    final Provider<MinefieldPresenter> minefieldPresenterProvider,
                    final Provider<MainPresenter> mainPresenterProvider) {

        nextMap.put(StartPresenter.class, scenarioPresenterProvider);
        nextMap.put(ScenarioPresenter.class, taskForcePresenterProvider);
        nextMap.put(TaskForcePresenter.class, minefieldPresenterProvider);
        nextMap.put(MinefieldPresenter.class, mainPresenterProvider);

        prevMap.put(ScenarioPresenter.class, startPresenterProvider);
        prevMap.put(TaskForcePresenter.class, scenarioPresenterProvider);
        prevMap.put(MinefieldPresenter.class, taskForcePresenterProvider);
        prevMap.put(MainPresenter.class, minefieldPresenterProvider);
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

}
