package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.presenter.airfield.AirfieldDialog;
import engima.waratsea.view.airfield.info.AirfieldRangeInfo;
import engima.waratsea.view.asset.AirfieldAssetSummaryView;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Singleton
public class AirfieldAssetPresenter {
    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;
    private final Provider<AirfieldDialog> airfieldDialogProvider;
    private final Provider<AirbaseViewModel> airbaseViewModelProvider;
    private final Provider<MainMapView> mainMapViewProvider;

    private final Set<String> hideAssets = new HashSet<>();  // Tracks which airfield asset views should be closed
    // When the corresponding airfield dialog is closed.

    /**
     * Constructor called by guice.
     *
     * @param assetSummaryViewProvider         Provides asset summary views.
     * @param airfieldAssetSummaryViewProvider Provides airfield asset summary views.
     * @param airbaseViewModelProvider         Provides airbase view model provider
     * @param airfieldDialogProvider           Provides airfield dialog boxes.
     * @param mainMapViewProvider              Provides the main map's view.
     */
    @Inject
    public AirfieldAssetPresenter(final Provider<AssetSummaryView> assetSummaryViewProvider,
                                  final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider,
                                  final Provider<AirfieldDialog> airfieldDialogProvider,
                                  final Provider<AirbaseViewModel> airbaseViewModelProvider,
                                  final Provider<MainMapView> mainMapViewProvider) {
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.airfieldAssetSummaryViewProvider = airfieldAssetSummaryViewProvider;
        this.airfieldDialogProvider = airfieldDialogProvider;
        this.airbaseViewModelProvider = airbaseViewModelProvider;
        this.mainMapViewProvider = mainMapViewProvider;
    }

    /**
     * Add an airfield to the asset summary.
     *
     * @param airfield The airfield to add.
     */
    public void addAirfieldToAssetSummary(final Airfield airfield) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airfield.getTitle());

        AirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airfield);

        AirfieldAssetSummaryView assetView = airfieldAssetSummaryViewProvider.get();
        assetView.build(viewModel);
        assetSummaryViewProvider.get().show(assetId, assetView);
        registerCallbacks(viewModel, assetView);
        selectFirstAircraftModel(assetView);
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
     * @param reset   Indicates if the asset's view model should be reset.
     */
    public void hide(final Airbase airbase, final boolean reset) {
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

        AirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airbase);

        AssetSummaryView assetManager = assetSummaryViewProvider.get();

        AirfieldAssetSummaryView assetView = (AirfieldAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseThrow();

        assetView.reset(viewModel);   // reset the airfield's asset summary's view of the airfield.
        registerCallbacks(viewModel, assetView);
        selectFirstAircraftModel(assetView);
    }

    /**
     * Register callbacks for airfield asset presenter.
     *
     * @param viewModel The airbase view model.
     * @param assetView The airfield asset summary view.
     */
    private void registerCallbacks(final AirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView) {
        assetView
                .getMissionButton()
                .setOnAction(this::airfieldManageMissions);

        assetView
                .getPatrolButton()
                .setOnAction(this::airfieldManagePatrols);

        assetView
                .getNationsTabPane()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldTab, newTab) -> nationSelected(viewModel, assetView, newTab));

        assetView
                .getRangeInfo()
                .forEach((nation, rangeInfoView) -> registerCallbacksForRange(rangeInfoView, nation, assetView, viewModel));
    }

    /**
     * Register the callbacks for the range controls.
     *
     * @param rangeInfoView The range info view.
     * @param nation        The nation.
     * @param assetView     The airfield asset summary view.
     * @param viewModel     The airbase view model.
     */
    private void registerCallbacksForRange(final AirfieldRangeInfo rangeInfoView,
                                           final Nation nation,
                                           final AirfieldAssetSummaryView assetView,
                                           final AirbaseViewModel viewModel) {
        rangeInfoView
                .getAircraftModels()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldAircraft, newAircraft) -> aircraftSelected(assetView, newAircraft));

        rangeInfoView
                .getConfig()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldConfig, newConfig) -> squadronConfigSelected(nation, viewModel, assetView, newConfig));

        rangeInfoView
                .getShowRangeOnMap()
                .selectedProperty()
                .addListener((v, oldValue, showRange) -> showRangeToggled(nation, viewModel, showRange));
    }

    /**
     * Select the first aircraft model in the list of aircraft models.
     *
     * @param assetView The airfield asset summary view.
     */
    private void selectFirstAircraftModel(final AirfieldAssetSummaryView assetView) {
        assetView
                .getRangeInfo()
                .values()
                .forEach(rangeInfoView -> rangeInfoView
                        .getAircraftModels()
                        .getSelectionModel()
                        .selectFirst());
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

        AirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airbase);

        AirfieldAssetSummaryView assetView = airfieldAssetSummaryViewProvider.get();
        assetView.build(viewModel);

        hideAssets.add(assetId.getKey());
        registerCallbacks(viewModel, assetView);
        selectFirstAircraftModel(assetView);

        return assetView;
    }

    /**
     * The aircraft in the range information pane is selected.
     *
     * @param assetView The airfield asset summary view.
     * @param aircraft  The selected aircraft.
     */
    private void aircraftSelected(final AirfieldAssetSummaryView assetView, final Aircraft aircraft) {
        if (aircraft != null) {
            assetView
                    .getRangeInfo()
                    .get(aircraft.getNationality())
                    .getConfig()
                    .getSelectionModel()
                    .selectFirst();
        }
    }

    /**
     * Callback for squadron configuration drop down selected.
     *
     * @param nation    The nation.
     * @param viewModel The airbase view model.
     * @param assetView The airfield asset summary view.
     * @param config    The selected squadron configuration.
     */
    private void squadronConfigSelected(final Nation nation, final AirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView, final SquadronConfig config) {
        if (config != null && assetView.getRangeInfo().get(nation).getShowRangeOnMap().isSelected()) {
            Nation tabNation = (Nation) assetView.getNationsTabPane().getSelectionModel().getSelectedItem().getUserData();

            if (tabNation == nation) {
                Airbase airbase = viewModel.getAirbaseModel();
                Aircraft aircraft = viewModel.getNationViewModels().get(nation).getSelectedAircraft().getValue();
                int range = aircraft.getRadius().get(config);

                mainMapViewProvider.get().drawRangeMarker(airbase, range);
            }
        }
    }

    /**
     * Show aircraft's range marker toggled.
     *
     * @param nation The nation of the range marker.
     * @param viewModel The airbase view model.
     * @param show If true the range marker is drawn. If false the range marker is hidden.
     */
    private void showRangeToggled(final Nation nation, final AirbaseViewModel viewModel, final boolean show) {
        Airbase airbase = viewModel.getAirbaseModel();

        if (show) {
            Aircraft aircraft = viewModel.getNationViewModels().get(nation).getSelectedAircraft().getValue();
            SquadronConfig config = viewModel.getNationViewModels().get(nation).getSelectedConfig().getValue();
            if (aircraft != null) {
                int range = aircraft.getRadius().get(config);
                mainMapViewProvider.get().drawRangeMarker(airbase, range);
            }
        } else {
            mainMapViewProvider.get().hideRangeMarker(airbase);
        }
    }

    /**
     * Callback for the nation tab selected.
     *
     * @param viewModel The airbase view model.
     * @param assetView The airfield asset summary view.
     * @param tab       The nation tab that is selected.
     */
    private void nationSelected(final AirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView, final Tab tab) {
        Nation nation = (Nation) tab.getUserData();

        Airbase airbase = viewModel.getAirbaseModel();
        mainMapViewProvider.get().hideRangeMarker(airbase);

        if (assetView.getRangeInfo().get(nation).getShowRangeOnMap().isSelected()) {
            Aircraft aircraft = viewModel.getNationViewModels().get(nation).getSelectedAircraft().getValue();
            SquadronConfig config = viewModel.getNationViewModels().get(nation).getSelectedConfig().getValue();
            int range = aircraft.getRadius().get(config);

            mainMapViewProvider.get().drawRangeMarker(airbase, range);
        }
    }
}
