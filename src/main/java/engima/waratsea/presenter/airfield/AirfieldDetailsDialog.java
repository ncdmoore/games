package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldDetailsView;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.squadron.SquadronViewType;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableRow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The presenter for the airfield details dialog.
 */
@Slf4j
public class AirfieldDetailsDialog {
    private static final String CSS_FILE = "airfieldDetails.css";

    private final MissionDAO missionDAO;
    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<AirfieldDetailsView> viewProvider;
    private final Provider<MainMapView> mapViewProvider;
    private final Provider<MissionAddDetailsDialog> missionAddDetailsDialogProvider;
    private final Provider<MissionEditDetailsDialog> missionEditDetailsDialogProvider;

    private final ViewProps props;

    private Stage stage;

    @Getter
    private AirfieldDetailsView view;
    private MainMapView mapView;

    private Airbase airbase;

    /**
     * Constructor called by guice.
     *
     * @param missionDAO The mission data access object.
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param mapViewProvider Provides the view of the main game map.
     * @param missionAddDetailsDialogProvider Provides the mission details add dialog.
     * @param missionEditDetailsDialogProvider Provides the mission details edit dialog.
     * @param props The view properties.
     */

    //CHECKSTYLE:OFF
    @Inject
    public AirfieldDetailsDialog(final MissionDAO missionDAO,
                                 final CssResourceProvider cssResourceProvider,
                                 final Provider<DialogView> dialogProvider,
                                 final Provider<AirfieldDetailsView> viewProvider,
                                 final Provider<MainMapView> mapViewProvider,
                                 final Provider<MissionAddDetailsDialog> missionAddDetailsDialogProvider,
                                 final Provider<MissionEditDetailsDialog> missionEditDetailsDialogProvider,
                                 final ViewProps props) {
    //CHECKSTYLE:ON
        this.missionDAO = missionDAO;
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.mapViewProvider = mapViewProvider;
        this.missionAddDetailsDialogProvider = missionAddDetailsDialogProvider;
        this.missionEditDetailsDialogProvider = missionEditDetailsDialogProvider;
        this.props = props;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param base The airfield for which the details are shown.
     */
    public void show(final Airbase base) {
        airbase = base;

        DialogView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();
        mapView = mapViewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " " +  airbase.getAirfieldType().getTitle() + " Details");

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(airbase));

        registerHandlers();

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());

        initializeMissionTable();

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Get a list of all the ready squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's ready squadrons.
     */
    public List<Squadron> getReady(final Nation nation) {
        return view
                .getAirfieldReadyView()
                .get(nation)
                .getReady();
    }

    /**
     * Initialize the mission table with copies of the airbase's missions.
     * This is done so that the missions can be edited, deleted and such
     * without affecting the model until the "Ok" button is clicked.
     */
    private void initializeMissionTable() {
        airbase.getNations().forEach(nation -> {
            //Make copies of the missions so they can be manipulated without affecting the data model
            //until the dialog ok button is clicked.
            List<AirMission> copies = airbase
                    .getMissions(nation)
                    .stream()
                    .map(AirMission::getData)
                    .map(data -> data.setAirbase(airbase))
                    .map(missionDAO::load)
                    .collect(Collectors.toList());

            view
                    .getAirfieldMissionView()
                    .get(nation)
                    .getTable()
                    .getItems()
                    .addAll(copies);
        });
    }

    /**
     * Add the given nation's given squadrn to the ready list.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The squadron to add.
     */
    private void addToReadyList(final Nation nation, final Squadron squadron) {
        view
                .getAirfieldReadyView()
                .get(nation)
                .add(squadron);
    }

    /**
     * Remove a given nation's given squadron from the ready list.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The squadron to remove.
     */
    private void removeFromReadyList(final Nation nation, final Squadron squadron) {
        view
                .getAirfieldReadyView()
                .get(nation)
                .remove(squadron);
    }

    /**
     * Add a given nation's given squadron to all the patrol available lists.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The squadron to add.
     */
    private void addToPatrolAvailableList(final Nation nation, final Squadron squadron) {
        view
                .getAirfieldPatrolView()
                .get(nation)
                .addSquadronToPatrolAvailableList(squadron);
    }
    /**
     * Remove a given nation's given squadron from all the patrol available lists.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param squadron The squadron to remove.
     */
    private void removeFromPatrolAvailableList(final Nation nation, final Squadron squadron) {
        view
                .getAirfieldPatrolView()
                .get(nation)
                .removeSquadronFromPatrolAvailableList(squadron);
    }

    /**
     * Register all handlers.
     */
    private void registerHandlers() {
        registerMissionHandlers();
        registerPatrolHandlers();
        registerReadyHandlers();
    }

    /**
     * Register the mission handlers for all nations..
     */
    private void registerMissionHandlers() {
        airbase.getNations().forEach(this::registerMissionHandlers);
    }

    /**
     * Register the mission handlers for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void registerMissionHandlers(final Nation nation) {
        MissionView missionView = view
                .getAirfieldMissionView()
                .get(nation);

        missionView.getAdd().setOnAction(this::missionAdd);
        missionView.getEdit().setOnAction(this::missionEdit);
        missionView.getDelete().setOnAction(this::missionDelete);

        // Handle table double clicks.
        missionView.getTable().setRowFactory(tv -> {
            TableRow<AirMission> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    missionEdit();
                }
            });
            return row;

        });
    }

    /**
     * Register the patrol handlers.
     **/
    private void registerPatrolHandlers() {
        airbase.getNations().forEach(nation -> {

            PatrolView patrolView = view
                    .getAirfieldPatrolView()
                    .get(nation);

            Stream.of(PatrolType.values()).forEach(patrolType -> {
                patrolView
                        .getAvailableList(patrolType)
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener((v, oldValue, newValue) -> patrolAvailableSquadronSelected(newValue, patrolType));

                patrolView
                        .getAssignedList(patrolType)
                        .getSelectionModel()
                        .selectedItemProperty()
                        .addListener((v, oldValue, newValue) -> patrolAssignedSquadronSelected(newValue, patrolType));

                patrolView
                        .getAddButton(patrolType)
                        .setOnAction(this::patrolAddSquadron);

                patrolView
                        .getRemoveButton(patrolType)
                        .setOnAction(this::patrolRemoveSquadron);
            });
        });
    }

    /**
     * Register handlers for when a squadron in a ready list is selected.
     ***/
    private void registerReadyHandlers() {
        airbase.getNations().forEach(nation ->
            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getReadyLists()
                    .forEach((type, listview) -> listview
                            .getSelectionModel()
                            .selectedItemProperty()
                            .addListener((v, oldValue, newValue) -> readySquadronSelected(newValue)))
        );
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        airbase.clearPatrolsAndMissions();
        updatePatrolsAndMissions();
        mapView.drawPatrolRadii(airbase);
        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }

    /**
     * Update this airbase with the current set of patrols and missions.
     */
    private void updatePatrolsAndMissions() {
        airbase.getNations().forEach(nation -> {
            updatePatrols(nation);
            updateMissions(nation);
        });
    }

    /**
     * Update the airbase with the current set of patrols.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void updatePatrols(final Nation nation) {
        Stream.of(PatrolType.values()).forEach(patrolType ->
            view.getAirfieldPatrolView()
                    .get(nation)
                    .getAssignedList(patrolType)
                    .getItems()
                    .forEach(squadron -> airbase
                    .getPatrol(patrolType)
                    .addSquadron(squadron)));
    }

    /**
     * Update the airbase with the current set of missions.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void updateMissions(final Nation nation) {
        view
                .getAirfieldMissionView()
                .get(nation)
                .getTable()
                .getItems()
                .forEach(mission -> airbase.addMission(mission));
    }

    /**
     * Add a mission to this airbase.
     *
     * @param event The button action event.
     */
    private void missionAdd(final ActionEvent event) {
        Nation nation = determineNation();

        MissionAddDetailsDialog dialog = missionAddDetailsDialogProvider.get();

        dialog
                .setNation(nation)
                .setParentDialog(this)
                .show(airbase);

        Optional
                .ofNullable(dialog.getMission())
                .ifPresent(mission -> {
                    view
                            .getAirfieldMissionView()
                            .get(nation)
                            .addMissionToTable(mission);

                    mission.getSquadrons().forEach(squadron -> {
                        removeFromReadyList(nation, squadron);
                        removeFromPatrolAvailableList(nation, squadron);
                        updateReadySummary(nation, squadron);
                    });

                });
    }

    /**
     * Edit a mission from this airbase.
     *
     * @param event The button action event.
     */
    private void missionEdit(final ActionEvent event) {
        missionEdit();
    }

    /**
     * Edit a mission from this airbase.
     */
    private void missionEdit() {
        Nation nation = determineNation();

        MissionEditDetailsDialog dialog = missionEditDetailsDialogProvider.get();

        AirMission mission = view
                .getAirfieldMissionView()
                .get(nation)
                .getTable()
                .getSelectionModel()
                .getSelectedItem();

        dialog.setMission(mission);

        dialog
                .setNation(nation)
                .setParentDialog(this)
                .show(airbase);

        AirMission updatedMission = dialog.getMission();

        List<Squadron> added = ListUtils.subtract(updatedMission.getSquadrons(), mission.getSquadrons());
        List<Squadron> removed = ListUtils.subtract(mission.getSquadrons(), updatedMission.getSquadrons());

        view
                .getAirfieldMissionView()
                .get(nation)
                .deleteMissionFromTable(mission);

        view
                .getAirfieldMissionView()
                .get(nation)
                .addMissionToTable(updatedMission);

        added.forEach(squadron -> {
            removeFromReadyList(nation, squadron);
            removeFromPatrolAvailableList(nation, squadron);
            updateReadySummary(nation, squadron);
        });

        removed.forEach(squadron -> {
            addToReadyList(nation, squadron);
            addToPatrolAvailableList(nation, squadron);
            updateReadySummary(nation, squadron);
        });
    }

    /**
     * Delete a mission to this airbase.
     *
     * @param event The button action event.
     */
    private void missionDelete(final ActionEvent event) {
        Nation nation = determineNation();

        AirMission mission = view
                .getAirfieldMissionView()
                .get(nation)
                .getTable()
                .getSelectionModel()
                .getSelectedItem();


        Optional.ofNullable(mission).ifPresent(deletedMission -> {
            view
                    .getAirfieldMissionView()
                    .get(nation)
                    .deleteMissionFromTable(deletedMission);

            deletedMission
                    .getSquadrons()
                    .forEach(squadron -> {
                        addToReadyList(nation, squadron);
                        addToPatrolAvailableList(nation, squadron);
                        updateReadySummary(nation, squadron);
                    });
        });

    }

    /**
     * Add a squadron to the corresponding patrol which is determined from the add button.
     *
     * @param event The button action event.
     */
    private void patrolAddSquadron(final ActionEvent event) {
        Button button = (Button) event.getSource();
        PatrolType type = (PatrolType) button.getUserData();

        Nation nation = determineNation();

        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .assignPatrol(type);

        removeFromPatrolAvailableList(nation, squadron);
        removeFromReadyList(nation, squadron);
        updatePatrolSummary(nation, type);
        updateReadySummary(nation, squadron);
        updatePatrolStats(nation, type);
    }

    /**
     * Remove a squadron from the corresponding patrol which is determined from the remove button.
     *
     * @param event The button action event.
     */
    private void patrolRemoveSquadron(final ActionEvent event) {
        Button button = (Button) event.getSource();
        PatrolType type = (PatrolType) button.getUserData();

        Nation nation = determineNation();

        Squadron squadron = view
                .getAirfieldPatrolView()
                .get(nation)
                .removePatrol(type);

        addToPatrolAvailableList(nation, type, squadron);
        addToReadyList(nation, squadron);
        updatePatrolSummary(nation, type);
        updateReadySummary(nation, squadron);
        updatePatrolStats(nation, type);
    }

    /**
     * A squadron from the given patrol type's available list has been selected.
     *
     * @param patrolSquadron The selected available squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAvailableSquadronSelected(final Squadron patrolSquadron, final PatrolType patrolType) {
        Optional.ofNullable(patrolSquadron).ifPresent(squadron -> {
            Nation nation = determineNation();

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAvailableSquadron(patrolSquadron, patrolType);
        });
    }

    /**
     * A squadron from the given patrol type's assigned list has been selected.
     *
     * @param patrolSquadron The selected assigned squadron.
     * @param patrolType The given patrol type.
     */
    private void patrolAssignedSquadronSelected(final Squadron patrolSquadron, final PatrolType patrolType) {
        Optional.ofNullable(patrolSquadron).ifPresent(squadron -> {
            Nation nation = determineNation();

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAssignedSquadron(patrolSquadron, patrolType);
        });
    }

    /**
     * Call back for a ready squadron selected.
     *
     * @param readySquadron The selected ready squadron.
     */
    private void readySquadronSelected(final Squadron readySquadron) {

        Optional.ofNullable(readySquadron).ifPresent(squadron -> {
            SquadronViewType type = SquadronViewType.get(readySquadron.getType());

            Nation nation = determineNation();

            //Clear all the other ready listview selections. If on clicking a listview
            //that already has a squadron selected and the same squadron is selected,
            //then no notification is sent. To avoid this we clear all other listviews
            //anytime a ready squadron is selected. This way when a listview is selected
            //a notification is guaranteed to be sent.
            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getReadyLists()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey() != type)
                    .forEach(entry -> entry.getValue().getSelectionModel().clearSelection());

            view
                    .getAirfieldReadyView()
                    .get(nation)
                    .getSquadronSummaryView()
                    .setSelectedSquadron(readySquadron);
        });
    }

    /**
     * Determine the active nation from the active tab.
     *
     * @return The active nation.
     */
    private Nation determineNation() {
        String selectedNation = view.getNationsTabPane()
                .getSelectionModel()
                .getSelectedItem()
                .getText();

        return Nation.get(selectedNation);
    }

    /**
     * Update the Patrol summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The patrol type.
     */
    private void updatePatrolSummary(final Nation nation, final PatrolType patrolType) {
        int numOnPatrol = view.getAirfieldPatrolView()
                .get(nation)
                .getNumSquadronsOnPatrol(patrolType);

        view.getAirfieldSummaryView()
                .get(nation)
                .updatePatrolSummary(patrolType, numOnPatrol);
    }

    /**
     * Update the ready summary.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param squadron The squadron that triggers the ready update.
     */
    private void updateReadySummary(final Nation nation, final Squadron squadron) {
        int numReady = view.getAirfieldReadyView()
                .get(nation)
                .getReady(SquadronViewType.get(squadron.getType()));

        view.getAirfieldSummaryView()
                .get(nation)
                .updateReadySummary(SquadronViewType.get(squadron.getType()), numReady);
    }

    /**
     * Update the patrol's stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @param patrolType The type of patrol.
     */
    private void updatePatrolStats(final Nation nation, final PatrolType patrolType) {
        view
                .getAirfieldPatrolView()
                .get(nation)
                .updatePatrolStats(nation, patrolType);
    }

    /**
     * Add a given nation's given squadron to all the patrol available lists, except for the specified
     * patrol type.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param patrolType The patrol type that is not updated.
     * @param squadron The squadron to add.
     */
    private void addToPatrolAvailableList(final Nation nation, final PatrolType patrolType, final Squadron squadron) {
        view.getAirfieldPatrolView()
                .get(nation)
                .addSquadronToPatrolAvailableList(patrolType, squadron);
    }
}
