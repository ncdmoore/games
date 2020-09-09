package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.view.map.marker.main.BaseMarker;
import lombok.Getter;

@Singleton
public class AssetPresenter {

    @Getter private final AirfieldAssetPresenter airfieldAssetPresenter;

    /**
     * Constructor called by guice.
     *
     * @param airfieldAssetPresenter The airfield asset presenter.
     */
    @Inject
    public AssetPresenter(final AirfieldAssetPresenter airfieldAssetPresenter) {
        this.airfieldAssetPresenter = airfieldAssetPresenter;
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
}
