package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.Aircraft;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.airfield.AirfieldDialog;
import engima.waratsea.view.airfield.info.AirfieldRangeInfo;
import engima.waratsea.view.asset.AirfieldAssetSummaryView;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.view.asset.AssetView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.viewmodel.airfield.RangeViewModel;
import engima.waratsea.viewmodel.airfield.RealAirbaseViewModel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Singleton
public class AirfieldAssetPresenter {
    private final Provider<AssetPresenter> assetPresenterProvider;
    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;
    private final Provider<AirfieldDialog> airfieldDialogProvider;
    private final Provider<RealAirbaseViewModel> airbaseViewModelProvider;
    private final Provider<MainMapView> mainMapViewProvider;

    private final Set<AssetId> hideAssets = new HashSet<>();  // Tracks which airfield asset views should be closed
                                                              // When the corresponding airfield dialog is closed.

    /**
     * Constructor called by guice.
     *
     * @param assetPresenterProvider           Provides asset presenter parent.
     * @param assetSummaryViewProvider         Provides asset summary views.
     * @param airfieldAssetSummaryViewProvider Provides airfield asset summary views.
     * @param airbaseViewModelProvider         Provides airbase view model provider
     * @param airfieldDialogProvider           Provides airfield dialog boxes.
     * @param mainMapViewProvider              Provides the main map's view.
     */
    @Inject
    public AirfieldAssetPresenter(final Provider<AssetPresenter> assetPresenterProvider,
                                  final Provider<AssetSummaryView> assetSummaryViewProvider,
                                  final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider,
                                  final Provider<AirfieldDialog> airfieldDialogProvider,
                                  final Provider<RealAirbaseViewModel> airbaseViewModelProvider,
                                  final Provider<MainMapView> mainMapViewProvider) {
        this.assetPresenterProvider = assetPresenterProvider;
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

        RealAirbaseViewModel viewModel = airbaseViewModelProvider
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
    public RealAirbaseViewModel getViewModel(final Airbase airbase) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());
        AssetSummaryView assetManager = assetSummaryViewProvider.get();

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

        if (hideAssets.contains(assetId)) {
            assetSummaryViewProvider.get().hide(assetId);
            hideAssets.remove(assetId);
        } else if (reset) {
            reset(airbase);
        }
    }

    /**
     * Select the given nation tab on the given airbase.
     *
     * @param nation The nation whose tab is selected.
     * @param airbase The airbase whose nation tab is selected.
     */
    public void setNation(final Nation nation, final Airbase airbase) {
        AssetSummaryView assetManager = assetSummaryViewProvider.get();
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        Optional<AssetView> airfieldAssetView = assetManager.getAsset(assetId);
        airfieldAssetView.ifPresent(asset -> ((AirfieldAssetSummaryView) asset).setNation(nation));
    }

    /**
     * Any changes that were not saved to the airfield need to be reflected in the asset summary view of
     * the airfield. Thus, the airfield's view model is reset to the data stored in the airfield's model.
     * This way the airfield's asset summary contains the current data from the model. This is only needed
     * when the airfield asset summary survives the dialog's cancel button, i.e., when the dialog does
     * not control the display of the airfield asset summary.
     *
     * @param airbase The airbase that was not saved.
     */
    private void reset(final Airbase airbase) {
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());

        RealAirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airbase);

        AssetSummaryView assetManager = assetSummaryViewProvider.get();

        AirfieldAssetSummaryView assetView = (AirfieldAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseThrow();

        assetView.reset(viewModel);   // reset the airfield's asset summary's view of the airfield.
        registerCallbacks(viewModel, assetView);
        selectFirstAircraftModel(assetView);

        resetDistantCap(airbase);
    }

    /**
     * Reset any task forces for which this airbase provides distant CAP.
     * This is needed when the airbase dialog box for the given airbase is cancelled.
     *
     * @param airbase The airbase providing distant CAP.
     */
    private void resetDistantCap(final Airbase airbase) {
        airbase
                .getMissions()
                .stream()
                .filter(m -> m.getType() == AirMissionType.DISTANT_CAP)            // Get the Distant CAP missions only.
                .map(AirMission::getTarget)
                .map(target -> (TaskForce) target.getView())
                .distinct()
                .forEach(this::resetTaskForce);
    }

    /**
     * Reset the given task force.
     *
     * @param taskForce A task force.
     */
    private void resetTaskForce(final TaskForce taskForce) {
        AssetPresenter assetPresenter = assetPresenterProvider.get();

        assetPresenter
                .getTaskForceAssetPresenter()
                .reset(taskForce);
    }


    /**
     * Register callbacks for airfield asset presenter.
     *
     * @param viewModel The airbase view model.
     * @param assetView The airfield asset summary view.
     */
    private void registerCallbacks(final RealAirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView) {
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
                                           final RealAirbaseViewModel viewModel) {
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

        RealAirbaseViewModel viewModel = airbaseViewModelProvider
                .get()
                .setModel(airbase);

        AirfieldAssetSummaryView assetView = airfieldAssetSummaryViewProvider.get();
        assetView.build(viewModel);

        hideAssets.add(assetId);
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
                    .get(aircraft.getNation())
                    .getConfig()
                    .getSelectionModel()
                    .selectFirst();
        }
    }

    /**
     * Callback for squadron configuration drop-down selected.
     *
     * @param nation    The nation.
     * @param viewModel The airbase view model.
     * @param assetView The airfield asset summary view.
     * @param config    The selected squadron configuration.
     */
    private void squadronConfigSelected(final Nation nation, final RealAirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView, final SquadronConfig config) {
        if (config != null && assetView.getRangeInfo().get(nation).getShowRangeOnMap().isSelected()) {
            Nation tabNation = (Nation) assetView.getNationsTabPane().getSelectionModel().getSelectedItem().getUserData();

            if (tabNation == nation) {
                Airbase airbase = viewModel.getAirbaseModel();
                Aircraft aircraft = viewModel.getNationViewModels().get(nation).getRangeViewModel().getSelectedAircraft().getValue();
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
    private void showRangeToggled(final Nation nation, final RealAirbaseViewModel viewModel, final boolean show) {
        Airbase airbase = viewModel.getAirbaseModel();

        if (show) {
            RangeViewModel rangeViewModel = viewModel
                    .getNationViewModels()
                    .get(nation)
                    .getRangeViewModel();

            Aircraft aircraft = rangeViewModel
                    .getSelectedAircraft()
                    .getValue();

            SquadronConfig config = rangeViewModel
                    .getSelectedConfig()
                    .getValue();

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
    private void nationSelected(final RealAirbaseViewModel viewModel, final AirfieldAssetSummaryView assetView, final Tab tab) {
        Nation nation = (Nation) tab.getUserData();

        Airbase airbase = viewModel.getAirbaseModel();
        mainMapViewProvider.get().hideRangeMarker(airbase);

        if (assetView.getRangeInfo().get(nation).getShowRangeOnMap().isSelected()) {
            RangeViewModel rangeViewModel = viewModel
                    .getNationViewModels()
                    .get(nation)
                    .getRangeViewModel();

            Aircraft aircraft = rangeViewModel
                    .getSelectedAircraft()
                    .getValue();

            SquadronConfig config = rangeViewModel
                    .getSelectedConfig()
                    .getValue();

            int range = aircraft.getRadius().get(config);

            mainMapViewProvider.get().drawRangeMarker(airbase, range);
        }
    }
}
