package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.view.map.marker.main.BaseMarker;
import engima.waratsea.view.map.marker.main.TaskForceMarker;
import lombok.Getter;

@Singleton
public class AssetPresenter {
    @Getter private final AirfieldAssetPresenter airfieldAssetPresenter;
    @Getter private final TaskForceAssetPresenter taskForceAssetPresenter;

    /**
     * Constructor called by guice.
     *
     * @param airfieldAssetPresenter The airfield asset presenter.
     * @param taskForceAssetPresenter The task force asset presenter.
     */
    @Inject
    public AssetPresenter(final AirfieldAssetPresenter airfieldAssetPresenter,
                          final TaskForceAssetPresenter taskForceAssetPresenter) {
        this.airfieldAssetPresenter = airfieldAssetPresenter;
        this.taskForceAssetPresenter = taskForceAssetPresenter;
    }

    /**
     * A human base marker selected.
     *
     * @param baseMarker The base marker that was selected.
     */
    public void humanBaseSelected(final BaseMarker baseMarker) {
        baseMarker
                .getBaseGrid()
                .getAirfield()
                .ifPresent(airfieldAssetPresenter::addAirfieldToAssetSummary);
    }

    /**
     * A human base marker unselected.
     *
     * @param baseMarker The base marker that was unselected.
     */
    public void humanBaseUnSelected(final BaseMarker baseMarker) {
        baseMarker
                .getBaseGrid()
                .getAirfield()
                .ifPresent(airfieldAssetPresenter::removeAirfieldFromAssetSummary);
    }

    /**
     * A human task force marker selected.
     *
     * @param taskForceMarker The task force marker that was selected.
     */
    public void humanTaskForceSelected(final TaskForceMarker taskForceMarker) {
        taskForceMarker
                .getTaskForceGrid()
                .getTaskForces()
                .forEach(taskForceAssetPresenter::addTaskForceToAssetSummary);
    }

    /**
     * A human task force marker unselected.
     *
     * @param taskForceMarker The task force marker that was unselected.
     */
    public void humanTaskForceUnSelected(final TaskForceMarker taskForceMarker) {
        taskForceMarker
                .getTaskForceGrid()
                .getTaskForces()
                .forEach(taskForceAssetPresenter::removeTaskForceFromAssetSummary);
    }
}
