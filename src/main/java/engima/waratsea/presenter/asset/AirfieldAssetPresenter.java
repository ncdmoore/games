package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.presenter.airfield.AirfieldDialog;
import engima.waratsea.view.asset.AirfieldAssetSummaryView;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.viewmodel.AirbaseViewModel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class AirfieldAssetPresenter {

    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;

    private final Provider<AirfieldDialog> airfieldDialogProvider;

    private final Provider<AirbaseViewModel> airbaseViewModelProvider;

    /**
     * Constructor called by guice.
     *
     * @param assetSummaryViewProvider Provides asset summary views.
     * @param airfieldAssetSummaryViewProvider Provides airfield asset summary views.
     * @param airbaseViewModelProvider Provides airbase view model provider
     * @param airfieldDialogProvider Provides airfield dialog boxes.
     */
    @Inject
    public AirfieldAssetPresenter(final Provider<AssetSummaryView> assetSummaryViewProvider,
                                  final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider,
                                  final Provider<AirfieldDialog> airfieldDialogProvider,
                                  final Provider<AirbaseViewModel> airbaseViewModelProvider) {
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.airfieldAssetSummaryViewProvider = airfieldAssetSummaryViewProvider;
        this.airfieldDialogProvider = airfieldDialogProvider;
        this.airbaseViewModelProvider = airbaseViewModelProvider;
    }

    /**
     * Add an airfield to the asset summary.
     *
     * @param airfield The airfield to add.
     */
    public void addAirfieldToAssetSummary(final Airfield airfield) {
        AirfieldAssetSummaryView assetView = airfieldAssetSummaryViewProvider.get();

        AirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airfield);

        assetView.build(viewModel);

        AssetId assetId = new AssetId(AssetType.AIRFIELD, airfield.getTitle());
        assetSummaryViewProvider.get().show(assetId, assetView);

        assetView.getMissionButton().setOnAction(this::airfieldManageMissions);
        assetView.getPatrolButton().setOnAction(this::airfieldManagePatrols);
    }

    /**
     * Remove an airfield from the asset summary.
     *
     * @param airfield The airfield to remove.
     */
    public void removeAirfieldFromAssetSummary(final Airfield airfield) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airfield.getTitle());
        assetSummaryViewProvider.get().hide(assetId);
    }



    /**
     * Callback for manage airfield mission button.
     *
     * @param event The button click event.
     */
    private void airfieldManageMissions(final ActionEvent event) {
        Button button = (Button) event.getSource();
        Airbase airbase = (Airbase) button.getUserData();

        airfieldDialogProvider.get().show(airbase, false);
    }

    /**
     * Callback for manage airfield patrol button.
     *
     * @param event The button click event.
     */
    private void airfieldManagePatrols(final ActionEvent event) {
        Button button = (Button) event.getSource();
        Airbase airbase = (Airbase) button.getUserData();

        airfieldDialogProvider.get().show(airbase, true);
    }
}
