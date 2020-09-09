package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
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

import java.util.HashSet;
import java.util.Set;

@Singleton
public class AirfieldAssetPresenter {

    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;

    private final Provider<AirfieldDialog> airfieldDialogProvider;

    private final Provider<AirbaseViewModel> airbaseViewModelProvider;

    private final Set<String> hideAssets = new HashSet<>();  // Tracks which airfield asset views should be closed
    // When the corresponding airfield dialog is closed.

    /**
     * Constructor called by guice.
     *
     * @param assetSummaryViewProvider         Provides asset summary views.
     * @param airfieldAssetSummaryViewProvider Provides airfield asset summary views.
     * @param airbaseViewModelProvider         Provides airbase view model provider
     * @param airfieldDialogProvider           Provides airfield dialog boxes.
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
     * Get an airfield's view model from the asset summary view.
     *
     * @param airbase The airbase for which the dialog is opened.
     * @return The airbase view model retrieved from the airfield asset view.
     */
    public AirbaseViewModel getViewModel(final Airbase airbase) {
        AssetSummaryView assetManager = assetSummaryViewProvider.get();
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        AirfieldAssetSummaryView airfieldAssetView = (AirfieldAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseGet(() -> addAirfieldToAssetView(airbase));

        assetManager.show(assetId, airfieldAssetView);

        return airfieldAssetView.getViewModel();
    }

    /**
     * Hide the airfield's asset summary.
     *
     * @param airbase The airbase whose asset should be hidden.
     * @param reset Inidicates if the asset's view model should be reset.
     */
    public void hide(final Airbase airbase, boolean reset) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        if (hideAssets.contains(assetId.getKey())) {
            assetSummaryViewProvider.get().hide(assetId);
            hideAssets.remove(assetId.getKey());
        } else if (reset) {
            reset(airbase);
        }
    }

    /**
     * Any changes that were not saved to the airfield need to be reflected in the asset summary view of
     * the airfield. Thus, the airfield's view model is reset to the data stored in the airfield's model.
     * This way the airfield's asset summary contains the current data from the model. This is only needed
     * when the airfield asset summary survives this dialog's cancel button, i.e., when this dialog does
     * not control the display of the airfield asset summary.
     *
     * @param airbase The airbase that was not saved.
     */
    private void reset(final Airbase airbase) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        AirbaseViewModel viewModel = airbaseViewModelProvider.get();
        viewModel.setModel(airbase);

        AssetSummaryView assetManager = assetSummaryViewProvider.get();

        AirfieldAssetSummaryView airfieldAssetView = (AirfieldAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseThrow();

        airfieldAssetView.reset(viewModel);   // reset the airfield's asset summary's view of the airfield.
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

    /**
     * Add an airfield asset view to the asset manager.
     *
     * @param airbase The airbase whose airfield asset view is added.
     * @return The added airfield asset view.
     */
    private AirfieldAssetSummaryView addAirfieldToAssetView(final Airbase airbase) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        AirbaseViewModel viewModel = airbaseViewModelProvider.get();
        viewModel.setModel(airbase);

        AirfieldAssetSummaryView airfieldAssetView = airfieldAssetSummaryViewProvider.get();
        airfieldAssetView.build(viewModel);

        hideAssets.add(assetId.getKey());

        return airfieldAssetView;
    }
}
