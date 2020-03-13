package engima.waratsea.presenter.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.presenter.airfield.AirfieldDetailsDialog;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionAddDetailsView;
import engima.waratsea.view.airfield.mission.MissionView;
import javafx.event.ActionEvent;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The presenter for the mission add dialog.
 */
@Slf4j
public class MissionAddDetailsDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final ImageResourceProvider imageResourceProvider;
    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionAddDetailsView> viewProvider;
    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;
    private AirfieldDetailsDialog airfieldDialog;

    private final Game game;
    private final MissionDAO missionDAO;
    private Airbase airbase;
    private Nation nation;

    @Getter
    private AirMission mission;

    private MissionType selectedMissionType;

    private MissionAddDetailsView view;

    private MissionDetails missionDetails;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param missionDAO Adds missions to air bases.
     * @param imageResourceProvider Provides images.
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     * @param missionDetails Mission details helper.
     */
    //CHECKSTYLE:OFF
    @Inject
    public MissionAddDetailsDialog(final Game game,
                                   final MissionDAO missionDAO,
                                   final ImageResourceProvider imageResourceProvider,
                                   final CssResourceProvider cssResourceProvider,
                                   final Provider<DialogView> dialogProvider,
                                   final Provider<MissionAddDetailsView> viewProvider,
                                   final ViewProps props,
                                   final MissionDetails missionDetails) {
        this.game = game;
        this.missionDAO = missionDAO;
        this.imageResourceProvider = imageResourceProvider;
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
        this.missionDetails = missionDetails;

    }
    //CHECKSTYLE:ON

    /**
     * Set the parent dialog.
     *
     * @param airfieldDetailsDialog The parent dialog.
     * @return This mission details dialog.
     */
    public MissionAddDetailsDialog setParentDialog(final AirfieldDetailsDialog airfieldDetailsDialog) {
        airfieldDialog = airfieldDetailsDialog;
        return this;
    }

    /**
     * Set the nation.
     *
     * @param currentNation The nation: BRITISH, ITALIAN, etc.
     * @return This mission details dialog.
     */
    public MissionAddDetailsDialog setNation(final Nation currentNation) {
        nation = currentNation;
        missionDetails.setNation(nation);
        return this;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param currentAirbase The air base.
     */
    public void show(final Airbase currentAirbase) {
        airbase = currentAirbase;
        missionDetails.setAirbase(airbase);

        dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        missionDetails.setView(view);

        Map<Nation, MissionView> missionView = airfieldDialog
                .getView()
                .getAirfieldMissionView();

        missionDetails.setMissionView(missionView);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " Mission Details");

        dialog.setWidth(props.getInt("mission.dialog.width"));
        dialog.setHeight(props.getInt("mission.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(nation, getValidMissionTypes()));

        registerHandlers();

        view.getMissionType().getSelectionModel().selectFirst();

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());

        dialog.getOkButton().setDisable(true);

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register the handlers for the mission dialog actions.
     */
    private void registerHandlers() {
        view
                .getMissionType()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> missionTypeSelected(newValue));

        view
                .getTarget()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> targetSelected(newValue));


        Stream.of(MissionRole.values()).forEach(role -> {
            view.getSquadronList(role)
                    .getAssigned()
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((v, oldValue, newValue) -> assignedSquadronSelected(newValue));

            view.getSquadronList(role)
                    .getAvailable()
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((v, oldValue, newValue) -> availableSquadronSelected(newValue));

            view
                    .getSquadronList(role)
                    .getAdd()
                    .setOnAction(this::addSquadron);

            view
                    .getSquadronList(role)
                    .getRemove()
                    .setOnAction(this::removeSquadron);
        });
    }

    /**
     * Callback when a mission type has been selected.
     *
     * @param missionType The selected mission type.
     */
    private void missionTypeSelected(final MissionType missionType) {
        List<Target> targets = game
                .getHumanPlayer()
                .getTargets(missionType, nation);

        selectedMissionType = missionType;

        //Filter out this airbase from the list of targets for FERRY missions.
        //No need in ferry aircraft to the same air base. That would accomplish nothing.
        targets = missionType == MissionType.FERRY ? filterThisAirbase(targets) : targets;

        clearNonMainRoleTabs();
        addNonMainRoleTabs();

        view.getImageView().setImage(imageResourceProvider.getImage(nation.toString() + missionType.toString() + ".png"));

        view.getTarget().getItems().clear();

        clearAllSquadrons();

        view.getTarget().getItems().addAll(targets);
        view.getTarget().getSelectionModel().selectFirst();

        setSquadronListTitles();
    }

    /**
     * Callback when a target has been selected.
     *
     * @param target The selected target.
     */
    private void targetSelected(final Target target) {

        missionDetails.setSelectedTarget(target);

        missionDetails.updateTargetView(mission, selectedMissionType);
        clearAllSquadrons();
        view.hideError();

        Optional.ofNullable(target).ifPresent(t -> {

            if (!missionDetails.hasCapacity()) {       // Check target for maximum squadron step capacity.
                view.showError(t.getTitle() + " is at max capacity");
                return;
            }

            if (!missionDetails.hasRegionCapacity()) { // Check target's region for maximum squadron step capacity.
                view.showError(t.getRegionTitle(nation) + " is at max capacity");
                return;
            }

            List<Squadron> availableSquadrons = airfieldDialog.getReady(nation);

            if (availableSquadrons.isEmpty()) {
                view.showError("No ready squadrons.");
                return;
            }

            availableSquadrons = availableSquadrons.stream()
                    .filter(t::mayAttack)
                    .collect(Collectors.toList());

            if (availableSquadrons.isEmpty()) {
                view.showError("No squadrons capable.");
                return;
            }

            availableSquadrons = availableSquadrons.stream()
                    .filter(t::inRange)
                    .collect(Collectors.toList());

            if (availableSquadrons.isEmpty()) {
                view.showError("No squadrons in range.");
                return;
            }

            setAvailableSquadrons(availableSquadrons);

        });
    }

    /**
     * An available squadron has been selected.
     *
     * @param squadron The available squadron.
     */
    private void availableSquadronSelected(final Squadron squadron) {
        Optional
                .ofNullable(squadron)
                .ifPresent(s -> {
                    MissionRole role = getSelectedRole();
                    view.getSquadronSummaryView().setSelectedSquadron(s);
                    view.getSquadronList(role).getAssigned().getSelectionModel().clearSelection();
                });

    }

    /**
     * An assigned squadron has been selected.
     *
     * @param squadron The available squadron.
     */
    private void assignedSquadronSelected(final Squadron squadron) {
        Optional
                .ofNullable(squadron)
                .ifPresent(s -> {
                    MissionRole role = getSelectedRole();
                    view.getSquadronSummaryView().setSelectedSquadron(s);
                    view.getSquadronList(role).getAvailable().getSelectionModel().clearSelection();
                });
    }

    /**
     * Add a squadron to the mission.
     *
     * @param event The button action event.
     */
    private void addSquadron(final ActionEvent event) {
        MissionRole role = getSelectedRole();

        missionDetails.getSelectedAvailableSquadron(role).ifPresent(squadron -> {
            if (missionDetails.mayAddSquadronToMission(mission, selectedMissionType, squadron)) {
                missionDetails.addSquadron(squadron, role);
                dialog.getOkButton().setDisable(false);
            }
        });
    }

    /**
     * Remove a squadron from the mission.
     *
     * @param event The button action event.
     */
    private void removeSquadron(final ActionEvent event) {
        MissionRole role = getSelectedRole();

        missionDetails.getSelectedAssignedSquadron(role).ifPresent(squadron -> {
            missionDetails.removeSquadron(role);
            if (view.getSquadronList(role).getAssigned().getItems().isEmpty()) {
                dialog.getOkButton().setDisable(true);
            }
        });
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        MissionType missionType = view.getMissionType().getSelectionModel().getSelectedItem();
        Target target = view.getTarget().getSelectionModel().getSelectedItem();
        List<Squadron> squadrons = view.getSquadronList(MissionRole.MAIN).getAssigned().getItems();
        List<Squadron> escort = view.getSquadronList(MissionRole.ESCORT).getAssigned().getItems();

        MissionData data = new MissionData();
        data.setNation(nation);
        data.setType(missionType);
        data.setTarget(target.getName());
        data.setAirbase(airbase);

        data.setSquadrons(squadrons
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList()));

        data.setEscort(escort
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList()));


        mission = missionDAO.load(data);

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }

    /**
     * Get the valid air base mission types.
     *
     * @return An array of air base mission types.
     */
    private MissionType[] getValidMissionTypes() {
        List<MissionType> types = new ArrayList<>(Arrays.asList(MissionType.values()));

        if (game.getHumanPlayer().getEnemyTaskForceTargets().isEmpty()) {
            types.remove(MissionType.NAVAL_TASK_FORCE_STRIKE);
        }

        return types.toArray(MissionType[]::new);
    }

    /**
     * Clear all of the squadron lists.
      */
    private void clearAllSquadrons() {
        Stream.
                of(MissionRole.values())
                .forEach(role -> view.getSquadronList(role).clearAll());
    }

    /**
     * Set the squadron list titles.
     */
    private void setSquadronListTitles() {
        Stream.of(MissionRole.values()).forEach(role -> {
            view.getSquadronList(role).setAvailableTitle(selectedMissionType + " " + role + " Available");
            view.getSquadronList(role).setAssignedTitle(selectedMissionType + " " + role + " Assigned");
        });
    }

    /**
     * Filter this airbase from the list of targets.
     *
     * @param targets A list of targets.
     * @return A list of targets without this airbase.
     */
    private List<Target> filterThisAirbase(final List<Target> targets) {
        return targets
                .stream()
                .filter(target -> !target.getName().equalsIgnoreCase(airbase.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Clear all mission squadron roles that are not the main mission role from the tab pane.
     */
    private void clearNonMainRoleTabs() {
        view.getRoleTabs()
                .entrySet()
                .stream()
                .filter(this::isNotMainRole)
                .forEach(this::removeTab);
    }

    /**
     * Given an entry where the key is a mission role and the value is a javafx tab,
     * return true if the mission role is not the main role.
     *
     * @param entry A map entry of mission role to javafx tab.
     * @return True if the mission role is not the main mission role.
     */
    private boolean isNotMainRole(final Map.Entry<MissionRole, Tab> entry) {
        return entry.getKey() != MissionRole.MAIN;
    }

    /**
     * Given an entry where the key is the mission role and the value is a javafx tab,
     * remove the javafx tab from its tab pane.
     *
     * @param entry A map entry of mission role to javafx tab.
     */
    private void removeTab(final Map.Entry<MissionRole, Tab> entry) {
        view.getTabPane().getTabs().remove(entry.getValue());
    }

    /**
     * Add all of the selected mission type's non main squadron roles to the tab pane.
     */
    private void addNonMainRoleTabs() {
        selectedMissionType
                .getRoles()
                .stream()
                .filter(this::isNotMainRole)
                .forEach(this::addTab);
    }

    /**
     * Determine if the given role is a main role.
     *
     * @param role A squadron mission role.
     * @return True if the role is the main role. False, otherwise.
     */
    private boolean isNotMainRole(final MissionRole role) {
        return role != MissionRole.MAIN;
    }

    /**
     * Add the corresponding tab for the given role to the tab pane.
     *
     * @param role A squadron mission role.
     */
    private void addTab(final MissionRole role) {
        Tab tab = view.getRoleTabs().get(role);
        view.getTabPane().getTabs().add(tab);
    }

    /**
     * Set the squadron lists starting available list.
     *
     * @param available The pool of available squadrons
     */
    private void setAvailableSquadrons(final List<Squadron> available) {
        selectedMissionType.getRoles().forEach(role -> {

            // Determine if the squadron is allowed to perform the mission role.
            List<Squadron> allowed = available
                    .stream()
                    .filter(squadron -> squadron.canDoRole(role))
                    .collect(Collectors.toList());

            view.getSquadronList(role).addAllToAvailable(allowed);
        });
    }

    /**
     * Determine the selected squadron mission role tab.
     *
     * @return The selected squadron mission role.
     */
    private MissionRole getSelectedRole() {
        return (MissionRole) view
                .getTabPane()
                .getSelectionModel()
                .getSelectedItem()
                .getUserData();
    }
}
