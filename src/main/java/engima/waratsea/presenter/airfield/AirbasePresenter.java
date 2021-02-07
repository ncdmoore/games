package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronState;
import engima.waratsea.presenter.airfield.mission.MissionAddDialog;
import engima.waratsea.presenter.airfield.mission.MissionEditDialog;
import engima.waratsea.view.airfield.AirfieldView;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import engima.waratsea.view.squadron.SquadronViewType;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import engima.waratsea.viewmodel.airfield.PatrolViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableRow;

/**
 * This class contains the controls for the airbase dialog: for both the airfield and task force dialogs.
 *
 * It is a place to put the common airbase controls.
 */
public class AirbasePresenter {
    private final Provider<AirfieldView> viewProvider;
    private final Provider<MissionAddDialog> missionAddDetailsDialogProvider;
    private final Provider<MissionEditDialog> missionEditDetailsDialogProvider;

    private AirbaseViewModel viewModel;

    private AirfieldView view;

    @Inject
    public AirbasePresenter(final Provider<AirfieldView> viewProvider,
                            final Provider<MissionAddDialog> missionAddDetailsDialogProvider,
                            final Provider<MissionEditDialog> missionEditDetailsDialogProvider) {
        this.viewProvider = viewProvider;
        this.missionAddDetailsDialogProvider = missionAddDetailsDialogProvider;
        this.missionEditDetailsDialogProvider = missionEditDetailsDialogProvider;
    }

    /**
     * Build the airbase view.
     *
     * @param airbaseVM The airbase view model.
     * @param showPatrols Indicates whether to show the patrol management pane on initial display of the airbase view.
     * @return The node containing the airbase view.
     */
    public Node build(final AirbaseViewModel airbaseVM, final boolean showPatrols) {
        view = viewProvider.get();
        viewModel = airbaseVM;

        view.setShowPatrolPane(showPatrols);

        Node node = view.build(viewModel);

        registerHandlers();

        return node;
    }

    /**
     * Register the nation tab changed callback handler.
     *
     * @param listener The listener for nation tab changes.
     */
    public void registerNationTabHandler(final ChangeListener<Tab> listener) {
        view
                .getNationsTabPane()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(listener);
    }

    /**
     * Register callback handlers.
     **/
    private void registerHandlers() {
        viewModel.getNations().forEach(this::registerMissionHandlers);
        viewModel.getNations().forEach(this::registerPatrolHandlers);
        viewModel.getNations().forEach(this::registerReadyHandlers);
        viewModel.getNations().forEach(this::registerAllHandlers);
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
     * Call back for the mission add button.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void missionAdd(final Nation nation) {
        missionAddDetailsDialogProvider
                .get()
                .show(getNationView(nation));
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

        getNationView(nation).removeMission(mission);
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
     * A squadron from the given patrol type's available list has been selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAvailableSquadronSelected(final Nation nation, final PatrolType patrolType, final SquadronViewModel squadron) {
        if (squadron != null) {
            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getAssigned(patrolType)
                    .getSelectionModel()
                    .clearSelection();

            PatrolViewModel patrolViewModel = getNationView(nation)
                    .getPatrolsViewModels()
                    .get(patrolType);

            SquadronConfig config = patrolViewModel.determineSquadronConfig();
            squadron.setConfig(config);

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getPatrolSummaryMap()
                    .get(patrolType)
                    .setSquadron(squadron);
        }
    }

    /**
     * A squadron from the given patrol type's assigned list has been selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAssignedSquadronSelected(final Nation nation, final PatrolType patrolType, final SquadronViewModel squadron) {
        if (squadron != null) {
            // Make sure the available squadron list has no selection.
            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getAvailable(patrolType)
                    .getSelectionModel()
                    .clearSelection();

            PatrolViewModel patrolViewModel = getNationView(nation)
                    .getPatrolsViewModels()
                    .get(patrolType);

            SquadronConfig config = patrolViewModel.determineSquadronConfig();
            squadron.setConfig(config);

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .getPatrolSummaryMap()
                    .get(patrolType)
                    .setSquadron(squadron);
        }
    }

    /**
     * Add a squadron to the corresponding patrol which is specified by the given patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param type The patrol type.
     */
    private void patrolAddSquadron(final Nation nation, final PatrolType type) {
        SquadronViewModel squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .getAvailable(type)
                .getSelectionModel()
                .getSelectedItem();


        getNationView(nation).addToPatrol(type, squadron);

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
        SquadronViewModel squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .getAssigned(type)
                .getSelectionModel()
                .getSelectedItem();

        getNationView(nation).removeFromPatrol(type, squadron);

        // Go ahead and pre select the next assigned squadron.
        // This allows the user to quickly remove several squadrons from
        // a patrol.
        view
                .getAirfieldPatrolView()
                .get(nation)
                .getAssigned(type)
                .getSelectionModel()
                .selectFirst();
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
     * Call back for a ready squadron selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param readySquadron The selected ready squadron.
     */
    private void readySquadronSelected(final Nation nation, final SquadronViewModel readySquadron) {
        if (readySquadron != null) {
            readySquadron.setConfig(SquadronConfig.NONE);

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

            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getSquadronSummaryView()
                    .setSquadron(readySquadron);
        }
    }

    /**
     * Call back for a ready squadron selected.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param allSquadron The selected ready squadron.
     */
    private void allSquadronSelected(final Nation nation, final SquadronViewModel allSquadron) {
        if (allSquadron != null) {
            SquadronViewType type = SquadronViewType.get(allSquadron.getType());

            SquadronState state = allSquadron.getState().getValue();

            // Since the configuration is set when an available (ready) squadron is selected in
            // either the patrol or mission screens, the configuration for any ready squadron
            // is reset to NONE when viewed on the 'all' squadrons screen.
            if (state == SquadronState.READY) {
                allSquadron.setConfig(SquadronConfig.NONE);
            }

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

            view
                    .getAirfieldAllView()
                    .get(nation)
                    .getSquadronSummaryView()
                    .setSquadron(allSquadron);

            view
                    .getAirfieldAllView()
                    .get(nation)
                    .setState(state);
        }
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

    private NationAirbaseViewModel getNationView(final Nation nation) {
        return viewModel.getNationViewModels().get(nation);
    }
}
