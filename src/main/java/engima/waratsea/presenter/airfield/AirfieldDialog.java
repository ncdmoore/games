package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.Rules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.presenter.airfield.mission.MissionAddDialog;
import engima.waratsea.presenter.airfield.mission.MissionEditDialog;
import engima.waratsea.presenter.asset.AssetPresenter;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldView;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents the airfield details dialog. This is were the airfield missions and patrols are assigned.
 */
@Slf4j
public class AirfieldDialog {
    private static final String CSS_FILE = "airfieldDetails.css";
    private static final Map<PatrolType, LinkedHashSet<SquadronConfig>> CONFIG_MAP = Map.of(
            PatrolType.ASW, new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE)),
            PatrolType.CAP,  new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE)),
            PatrolType.SEARCH,  new LinkedHashSet<>(Arrays.asList(SquadronConfig.SEARCH, SquadronConfig.NONE)));

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<AirfieldView> viewProvider;
    private final Provider<MainMapView> mapViewProvider;
    private final Provider<MissionAddDialog> missionAddDetailsDialogProvider;
    private final Provider<MissionEditDialog> missionEditDetailsDialogProvider;
    private final AssetPresenter assetPresenter;

    private final ViewProps props;
    private final Rules rules;
    private Stage stage;

    @Getter
    private AirfieldView view;
    private MainMapView mapView;

    private Airbase airbase;

    private AirbaseViewModel viewModel;
    private Map<Nation, NationAirbaseViewModel> viewModelMap;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param mapViewProvider Provides the view of the main game map.
     * @param missionAddDetailsDialogProvider Provides the mission details add dialog.
     * @param missionEditDetailsDialogProvider Provides the mission details edit dialog.
     * @param assetPresenter Provides the asset presenters.
     * @param props The view properties.
     * @param rules The game rules.
     */

    //CHECKSTYLE:OFF
    @Inject
    public AirfieldDialog(final CssResourceProvider cssResourceProvider,
                          final Provider<DialogView> dialogProvider,
                          final Provider<AirfieldView> viewProvider,
                          final Provider<MainMapView> mapViewProvider,
                          final Provider<MissionAddDialog> missionAddDetailsDialogProvider,
                          final Provider<MissionEditDialog> missionEditDetailsDialogProvider,
                          final AssetPresenter assetPresenter,
                          final ViewProps props,
                          final Rules rules) {
        //CHECKSTYLE:ON
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.mapViewProvider = mapViewProvider;
        this.missionAddDetailsDialogProvider = missionAddDetailsDialogProvider;
        this.missionEditDetailsDialogProvider = missionEditDetailsDialogProvider;
        this.assetPresenter = assetPresenter;
        this.props = props;
        this.rules = rules;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param base The airfield for which the details are shown.
     * @param showPatrols Set to true to show the patrols pane by default.
     *                    Otherwise, the mission pane is shown.
     */
    public void show(final Airbase base, final boolean showPatrols) {
        airbase = base;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " " +  airbase.getAirfieldType().getTitle() + " Details");

        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        view = viewProvider.get();
        mapView = mapViewProvider.get();

        viewModel = assetPresenter
                .getAirfieldAssetPresenter()
                .getViewModel(airbase);

        viewModelMap = viewModel.getNationViewModels();

        view.setShowPatrolPane(showPatrols);
        dialog.setContents(view.build(viewModelMap));

        registerHandlers(dialog);

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register callback handlers.
     *
     * @param dialog This dialog's view.
     */
    private void registerHandlers(final DialogView dialog) {
        airbase.getNations().forEach(this::registerMissionHandlers);
        airbase.getNations().forEach(this::registerPatrolHandlers);
        airbase.getNations().forEach(this::registerReadyHandlers);
        airbase.getNations().forEach(this::registerAllHandlers);

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());
    }


    /**
     * Register the mission handlers for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void registerMissionHandlers(final Nation nation) {
        MissionView missionView = view.getAirfieldMissionView().get(nation);

        missionView.getAdd().setOnAction(event -> missionAdd(nation));
        missionView.getEdit().setOnAction(event -> missionEdit(nation));
        missionView.getDelete().setOnAction(event -> missionDelete(nation));

        // Handle table double clicks.
        missionView.getTable().setRowFactory(tv -> {
            TableRow<AirMissionViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    missionEdit(nation);
                }
            });
            return row;

        });
    }
    /**
     * Register patrol callbacks for all patrol types for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void registerPatrolHandlers(final Nation nation) {
       PatrolType
               .stream()
               .forEach(type -> registerPatrolHandler(nation, type));
    }

    /**
     * Register the callbacks for the given nation and given patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param patrolType The patrol type.
     */
    private void registerPatrolHandler(final Nation nation, final PatrolType patrolType) {
        PatrolView patrolView = view.getAirfieldPatrolView().get(nation);

        patrolView
                .getAvailable(patrolType)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> patrolAvailableSquadronSelected(nation, patrolType, nv));

        patrolView
                .getAssigned(patrolType)
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> patrolAssignedSquadronSelected(nation, patrolType, nv));

        patrolView
                .getAddButton(patrolType)
                .setOnAction(event -> patrolAddSquadron(nation, patrolType));

        patrolView
                .getRemoveButton(patrolType)
                .setOnAction(event -> patrolRemoveSquadron(nation, patrolType));
    }

    /**
     * Register callback handlers for when a squadron in the ready list is selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void registerReadyHandlers(final Nation nation) {
        view
                .getAirfieldReadyView()
                .get(nation)
                .getSquadrons()
                .values()
                .forEach(lv -> registerHandler(lv, (o, ov, nv) -> readySquadronSelected(nation, nv)));
    }

    /**
     * Register callback handlers for when a squadron in the all list is selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void registerAllHandlers(final Nation nation) {
        view
                .getAirfieldAllView()
                .get(nation)
                .getSquadrons()
                .values()
                .forEach(lv -> registerHandler(lv, (o, ov, nv) -> allSquadronSelected(nation, nv)));
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        viewModel.savePatrols();
        viewModel.saveMissions();

        mapView.toggleBaseMarkers(airbase);

        assetPresenter
                .getAirfieldAssetPresenter()
                .hide(airbase, false);

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        assetPresenter
                .getAirfieldAssetPresenter()
                .hide(airbase, true);

        mapView.toggleBaseMarkers(airbase);

        stage.close();
    }

    /**
     * Call back for the mission add button.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void missionAdd(final Nation nation) {
        missionAddDetailsDialogProvider
                .get()
                .show(viewModelMap.get(nation));
    }

    /**
     * Call back for the mission edit button.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void missionEdit(final Nation nation) {
        AirMissionViewModel mission = view
                .getAirfieldMissionView()
                .get(nation)
                .getTable()
                .getSelectionModel()
                .getSelectedItem();

        missionEditDetailsDialogProvider
                .get()
                .show(mission);
    }

    /**
     * Call back for the mission delete button.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void missionDelete(final Nation nation) {
        AirMissionViewModel mission = view
                .getAirfieldMissionView()
                .get(nation)
                .getTable()
                .getSelectionModel()
                .getSelectedItem();

        view
                .getAirfieldMissionView()
                .get(nation)
                .deleteMissionFromTable(mission);

        viewModelMap.get(nation).removeMission(mission);
    }

    /**
     * A squadron from the given patrol type's available list has been selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAvailableSquadronSelected(final Nation nation, final PatrolType patrolType, final Squadron squadron) {
        if (squadron != null) {
            SquadronConfig config = determineConfiguration(patrolType);

            squadron.setConfig(config);

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getAssigned(patrolType)
                    .getSelectionModel()
                    .clearSelection();

            viewModelMap
                    .get(nation)
                    .getPatrols()
                    .get(patrolType)
                    .getSelectedSquadron()
                    .get(nation)
                    .set(squadron, config);
        }
    }

    /**
     * A squadron from the given patrol type's assigned list has been selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAssignedSquadronSelected(final Nation nation, final PatrolType patrolType, final Squadron squadron) {
        if (squadron != null) {
            SquadronConfig config = determineConfiguration(patrolType);

            squadron.setConfig(config);

            // Make sure the available squadron list has no selection.
            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getAvailable(patrolType)
                    .getSelectionModel()
                    .clearSelection();


            viewModelMap
                    .get(nation)
                    .getPatrols()
                    .get(patrolType)
                    .getSelectedSquadron()
                    .get(nation)
                    .set(squadron, config);
        }
    }

    /**
     * Add a squadron to the corresponding patrol which is specified by the given patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param type The patrol type.
     */
    private void patrolAddSquadron(final Nation nation, final PatrolType type) {
        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .getAvailable(type)
                .getSelectionModel()
                .getSelectedItem();


        viewModelMap
                .get(nation)
                .addToPatrol(type, squadron);

        // Go ahead and pre select the next available squadron.
        // This allows the user to quickly add several squadrons to
        // a patrol.
        view
                .getAirfieldPatrolView()
                .get(nation)
                .getAvailable(type)
                .getSelectionModel()
                .selectFirst();
    }

    /**
     * Remove a squadron from the corresponding patrol which is specified by the given patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param type The patrol type.
     */
    private void patrolRemoveSquadron(final Nation nation, final PatrolType type) {
        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .getAssigned(type)
                .getSelectionModel()
                .getSelectedItem();

        viewModelMap
                .get(nation)
                .removeFromPatrol(type, squadron);
    }

    /**
     * Call back for a ready squadron selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param readySquadron The selected ready squadron.
     */
    private void readySquadronSelected(final Nation nation, final Squadron readySquadron) {
        if (readySquadron != null) {
            SquadronViewType type = SquadronViewType.get(readySquadron.getType());

            //Clear all the other ready listView selections. If on clicking a listView
            //that already has a squadron selected and the same squadron is selected,
            //then no notification is sent. To avoid this we clear all other listViews
            //anytime a ready squadron is selected. This way when a listView is selected
            //a notification is guaranteed to be sent.
            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getSquadrons()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != type)
                    .forEach(entry -> entry.getValue().getSelectionModel().clearSelection());

            viewModelMap
                    .get(nation)
                    .getSquadronStateViewModel()
                    .get(SquadronState.READY)
                    .getSelectedSquadron()
                    .set(readySquadron);
        }
    }

    /**
     * Call back for a ready squadron selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param allSquadron The selected ready squadron.
     */
    private void allSquadronSelected(final Nation nation, final Squadron allSquadron) {
        if (allSquadron != null) {
            SquadronViewType type = SquadronViewType.get(allSquadron.getType());
            SquadronState state = viewModelMap.get(nation).determineSquadronState(allSquadron);

            //Clear all the other all listView selections. If on clicking a listView
            //that already has a squadron selected and the same squadron is selected,
            //then no notification is sent. To avoid this we clear all other listViews
            //anytime an all squadron is selected. This way when a listView is selected
            //a notification is guaranteed to be sent.
            view
                    .getAirfieldAllView()
                    .get(nation)
                    .getSquadrons()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != type)
                    .forEach(entry -> entry.getValue().getSelectionModel().clearSelection());

            viewModelMap
                    .get(nation)
                    .getSquadronStateViewModel()
                    .get(SquadronState.ALL)
                    .getSelectedSquadron()
                    .set(allSquadron, state);

            view
                    .getAirfieldAllView()
                    .get(nation)
                    .setState(state);
        }
    }

    /**
     * Determine the best squadron configuration for the given type of patrol.
     *
     * @param patrolType The type of patrol.
     * @return The best squadron configuration for the given type of patrol.
     */
    private SquadronConfig determineConfiguration(final PatrolType patrolType) {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO().setPatrolType(PatrolType.SEARCH);

        Set<SquadronConfig> allowed = rules.getAllowedSquadronConfig(dto);

        // Get the first config for the given patrol type that is allowed.
        // This should return the most desired patrol squadron configuration.
        return CONFIG_MAP
                .get(patrolType)
                .stream()
                .filter(allowed::contains)
                .findFirst()
                .orElse(SquadronConfig.NONE);
    }

    /**
     * Utility for registering callback for listview selections.
     *
     * @param t list view
     * @param listener the callback lambda
     * @param <T> The type of objects in the list view.
     */
    private <T> void registerHandler(final ListView<T> t, final ChangeListener<T> listener) {
        t
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(listener);
    }
}
