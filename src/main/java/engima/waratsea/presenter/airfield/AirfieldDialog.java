package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.rules.Rules;
import engima.waratsea.model.game.rules.SquadronConfigRulesDTO;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.presenter.airfield.mission.MissionAddDialog;
import engima.waratsea.presenter.airfield.mission.MissionEditDialog;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.AirfieldView;
import engima.waratsea.view.airfield.mission.MissionView;
import engima.waratsea.view.airfield.patrol.PatrolView;
import engima.waratsea.view.asset.AirfieldAssetSummaryView;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The presenter for the airfield details dialog.
 */
@Slf4j
public class AirfieldDialog {
    private static final String CSS_FILE = "airfieldDetails.css";
    private static final Map<PatrolType, LinkedHashSet<SquadronConfig>> CONFIG_MAP = Map.of(
            PatrolType.ASW, new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE)),
            PatrolType.CAP,  new LinkedHashSet<>(Collections.singletonList(SquadronConfig.NONE)),
            PatrolType.SEARCH,  new LinkedHashSet<>(Arrays.asList(SquadronConfig.SEARCH, SquadronConfig.NONE)));

    private final MissionDAO missionDAO;
    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<AirfieldView> viewProvider;
    private final Provider<MainMapView> mapViewProvider;
    private final Provider<MissionAddDialog> missionAddDetailsDialogProvider;
    private final Provider<MissionEditDialog> missionEditDetailsDialogProvider;
    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider;

    private final ViewProps props;
    private final Rules rules;
    private Stage stage;

    @Getter private AirfieldView view;
    private MainMapView mapView;

    private Airbase airbase;
    private boolean hideAssetSummary; // Indicates if the asset view for this airfield should be closed when the
                                      // dialog is closed.

    private AirfieldAssetSummaryView airfieldAssetView;

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
     * @param assetSummaryViewProvider Provides the asset summary view pane.
     * @param airfieldAssetSummaryViewProvider Provides the airfield data to place in the asset summary view.
     * @param props The view properties.
     * @param rules The game rules.
     */

    //CHECKSTYLE:OFF
    @Inject
    public AirfieldDialog(final MissionDAO missionDAO,
                          final CssResourceProvider cssResourceProvider,
                          final Provider<DialogView> dialogProvider,
                          final Provider<AirfieldView> viewProvider,
                          final Provider<MainMapView> mapViewProvider,
                          final Provider<MissionAddDialog> missionAddDetailsDialogProvider,
                          final Provider<MissionEditDialog> missionEditDetailsDialogProvider,
                          final Provider<AssetSummaryView> assetSummaryViewProvider,
                          final Provider<AirfieldAssetSummaryView> airfieldAssetSummaryViewProvider,
                          final ViewProps props,
                          final Rules rules) {
    //CHECKSTYLE:ON
        this.missionDAO = missionDAO;
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.mapViewProvider = mapViewProvider;
        this.missionAddDetailsDialogProvider = missionAddDetailsDialogProvider;
        this.missionEditDetailsDialogProvider = missionEditDetailsDialogProvider;
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.airfieldAssetSummaryViewProvider = airfieldAssetSummaryViewProvider;
        this.props = props;
        this.rules = rules;
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

        showAssetSummary();

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
     * Add the given nation's given squadron to the ready list.
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
                    .forEach((type, listView) -> listView
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

        if (hideAssetSummary) {
            AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());
            assetSummaryViewProvider
                    .get()
                    .hide(assetId);
        }

        airfieldAssetView.update();
        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        if (hideAssetSummary) {
            AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());
            assetSummaryViewProvider
                    .get()
                    .hide(assetId);
        }

        airfieldAssetView.update();
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

        MissionAddDialog dialog = missionAddDetailsDialogProvider.get();

        dialog
                .setNation(nation)
                .setParentDialog(this)
                .show(airbase);

        //Once the dialog is closed the following code is executed.
        Optional
                .ofNullable(dialog.getMission())
                .ifPresent(mission -> {
                    view
                            .getAirfieldMissionView()
                            .get(nation)
                            .addMissionToTable(mission);

                    mission.getSquadronsAllRoles().forEach(squadron -> {
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

        MissionEditDialog dialog = missionEditDetailsDialogProvider.get();

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

        List<Squadron> added = ListUtils.subtract(updatedMission.getSquadronsAllRoles(), mission.getSquadronsAllRoles());
        List<Squadron> removed = ListUtils.subtract(mission.getSquadronsAllRoles(), updatedMission.getSquadronsAllRoles());

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
                    .getSquadronsAllRoles()
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
            SquadronConfig config = determineConfiguration(squadron, patrolType);

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAvailableSquadron(patrolSquadron, config, patrolType);
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
            SquadronConfig config = determineConfiguration(squadron, patrolType);

            view
                    .getAirfieldPatrolView()
                    .get(nation)
                    .selectAssignedSquadron(patrolSquadron, config, patrolType);
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

            //Clear all the other ready listView selections. If on clicking a listView
            //that already has a squadron selected and the same squadron is selected,
            //then no notification is sent. To avoid this we clear all other listViews
            //anytime a ready squadron is selected. This way when a listView is selected
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

        airfieldAssetView.updatePatrol(patrolType, numOnPatrol);
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

    /**
     * Determine the best squadron configuration for the given type of patrol.
     *
     * @param squadron The squadron on patrol.
     * @param patrolType The type of patrol.
     * @return The best squadron configuration for the given type of patrol.
     */
    private SquadronConfig determineConfiguration(final Squadron squadron, final PatrolType patrolType) {
        SquadronConfigRulesDTO dto = new SquadronConfigRulesDTO()
                .setPatrolType(PatrolType.SEARCH);

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
     * Show the airfield asset summary.
     */
    private void showAssetSummary() {
        AssetSummaryView assetManager = assetSummaryViewProvider.get();
        AssetId assetId = new AssetId(AssetType.AIRFIELD, airbase.getTitle());
        airfieldAssetView = (AirfieldAssetSummaryView) assetManager.getAsset(assetId);

        if (airfieldAssetView == null) {
            airfieldAssetView = airfieldAssetSummaryViewProvider.get();

            airfieldAssetView.build();
            airfieldAssetView.show(airbase);

            assetManager.show(assetId, airfieldAssetView);
            hideAssetSummary = true;
        } else {
            hideAssetSummary = false;
        }
    }
}
