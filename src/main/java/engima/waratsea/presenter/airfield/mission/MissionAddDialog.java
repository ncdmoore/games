package engima.waratsea.presenter.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.airfield.mission.AirMissionType;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.WarnDialog;
import engima.waratsea.view.airfield.mission.MissionAddView;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import engima.waratsea.viewmodel.airfield.NationAirbaseViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.scene.control.Tab;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
@Slf4j
public class MissionAddDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionAddView> viewProvider;
    private final Provider<AirMissionViewModel> missionViewModelProvider;
    private final Provider<WarnDialog> warnDialogProvider;

    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;

    private AirMissionViewModel viewModel;

    @Getter private AirMissionType selectedMissionType;
    @Getter private Target selectedTarget;

    private MissionAddView view;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param missionViewModelProvider Provides mission view models.
     * @param warnDialogProvider Provides warning dialogs.
     * @param props The view properties.
     */
    @Inject
    public MissionAddDialog(final CssResourceProvider cssResourceProvider,
                            final Provider<DialogView> dialogProvider,
                            final Provider<MissionAddView> viewProvider,
                            final Provider<AirMissionViewModel> missionViewModelProvider,
                            final Provider<WarnDialog> warnDialogProvider,
                            final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.missionViewModelProvider = missionViewModelProvider;
        this.warnDialogProvider = warnDialogProvider;
        this.props = props;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param nationAirbaseViewModel The air base.
     */
    public void show(final NationAirbaseViewModel nationAirbaseViewModel) {
        Nation nation = nationAirbaseViewModel.getNation();

        dialog = dialogProvider.get();             // The dialog view that contains the airfield details view.
        view = viewProvider.get();                 // The mission add view.
        viewModel = missionViewModelProvider       // The air mission view model.
                .get()
                .setNation(nation)
                .setSquadrons(nationAirbaseViewModel.getSquadrons())
                .setNationViewModel(nationAirbaseViewModel);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(nationAirbaseViewModel.getTitle().getValue() + " Mission Details");

        dialog.setWidth(props.getInt("mission.dialog.width"));
        dialog.setHeight(props.getInt("mission.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.build(nation).bind(viewModel));

        registerHandlers();

        view.getMissionType().getSelectionModel().selectFirst();

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register call back handlers.
     */
    private void registerHandlers() {
        view
                .getMissionType()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, oldValue, newValue) -> missionTypeSelected(newValue));

        view
                .getTarget()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, oldValue, newValue) -> targetSelected(newValue));

        MissionRole
                .stream()
                .forEach(this::registerListHandlers);

        viewModel.getWarning().addListener((o, ov, nv) -> warningHandler(nv));

        registerButtons();

        dialog.getOkButton().disableProperty().bind(viewModel.getValidMission().not());

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());
    }

    /**
     * Register buttons.
     */
    private void registerButtons() {
        MissionRole.stream().forEach(role -> {
            view.getSquadrons().get(role).getAdd().setOnAction(event -> assignSquadron(role));
            view.getSquadrons().get(role).getRemove().setOnAction(event -> removeSquadron(role));
        });
    }

    /**
     * Register the list handlers.
     *
     * @param role The mission role.
     */
    private void registerListHandlers(final MissionRole role) {
        viewModel
                .getError()
                .get(role)
                .addListener((o, ov, nv) -> errorHandler(role, nv));

        view
                .getSquadronList(role)
                .getAvailable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> availableSquadronSelected(role, nv));

        view
                .getSquadronList(role)
                .getAssigned()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((o, ov, nv) -> assignedSquadronSelected(role, nv));
    }

    /**
     * Callback for when the mission type selected.
     *
     * @param missionType The selected mission type.
     */
    private void missionTypeSelected(final AirMissionType missionType) {
        selectedMissionType = missionType;

        viewModel.setMissionType(missionType);

        view.getTarget().getItems().clear();
        view.getTarget().getItems().addAll(viewModel.getAvailableTargets());
        view.getTarget().getSelectionModel().selectFirst();
        view.getTarget().setDisable(viewModel.getAvailableTargets().isEmpty());

        setMainRoleTabText();
        clearNonMainRoleTabs();
        addNonMainRoleTabs();
    }

    /**
     * Callback for when the target is selected.
     *
     * @param target The selected target.
     */
    private void targetSelected(final Target target) {
        if (target != null) {
            selectedTarget = target;
            viewModel.clearMission();
            viewModel.setTarget(target);
        }
    }

    /**
     * An available squadron has been selected.
     *
     * @param role The mission role.
     * @param squadron The available squadron.
     */
    private void availableSquadronSelected(final MissionRole role, final SquadronViewModel squadron) {
        if (squadron != null) {
            // Go ahead and set the configuration so that it is consistently shown on the mission.
            // This way the configuration is the same if the squadron is in the available list
            // as it is in the assigned list.
            squadron.setConfig(selectedTarget, selectedMissionType, role);

            view
                    .getSquadronSummaryView()
                    .setSquadron(squadron);

            view
                    .getSquadronList(role)
                    .getAssigned()
                    .getSelectionModel()
                    .clearSelection();
        }
    }

    /**
     * An assigned squadron has been selected.
     *
     * @param role The mission role.
     * @param squadron The available squadron.
     */
    private void assignedSquadronSelected(final MissionRole role, final SquadronViewModel squadron) {
        if (squadron != null) {
            // Go ahead and set the configuration so that it is consistently shown on the mission.
            // This way the configuration is the same if the squadron is in the available list
            // as it is in the assigned list.
            squadron.setConfig(selectedTarget, selectedMissionType, role);

            view
                    .getSquadronSummaryView()
                    .setSquadron(squadron);

            view
                    .getSquadronList(role)
                    .getAvailable()
                    .getSelectionModel()
                    .clearSelection();
        }
    }

    /**
     * Assign a squadron with the given role to this mission.
     *
     * @param role The mission role.
     */
    private void assignSquadron(final MissionRole role) {
        SquadronViewModel squadron = view
                .getSquadrons()
                .get(role)
                .getAvailable()
                .getSelectionModel()
                .getSelectedItem();

        if (squadron != null) {
            viewModel.addToMission(squadron, role);

            // Go ahead and pre-select the next available squadron.
            // This allows a user to quickly add squadrons to a mission.
            view
                    .getSquadrons()
                    .get(role)
                    .getAvailable()
                    .getSelectionModel()
                    .selectFirst();
        }
    }

    /**
     * Remove a squadron with the given role from this mission.
     *
     * @param role The mission role.
     */
    private void removeSquadron(final MissionRole role) {
        SquadronViewModel squadron = view
                .getSquadrons()
                .get(role)
                .getAssigned()
                .getSelectionModel()
                .getSelectedItem();

        if (squadron != null) {
            viewModel.removeFromMission(squadron, role);

            // Go ahead and pre-select the next assigned squadron.
            // This allows a user to quickly remove squadrons from a mission.
            view
                    .getSquadrons()
                    .get(role)
                    .getAssigned()
                    .getSelectionModel()
                    .selectFirst();
        }
    }

    /**
     * Callback for when the view model has encountered an error.
     *
     * @param role The mission role.
     * @param error If true indicates an error occurred.
     */
    private void errorHandler(final MissionRole role, final Boolean error) {
        if (error) {
            view.showError(role, viewModel.getErrorText().get(role));
        } else {
            view.hideError(role);
        }
    }

    /**
     * Callback for when the view model has encountered a warning.
     *
     * @param warning If true indicates a warning occurred.
     */
    private void warningHandler(final Boolean warning) {
        if (warning) {
            warnDialogProvider.get().show(viewModel.getWarningText());
        }
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        viewModel.createMission();
        stage.close();
    }

    /**
     * Set the main mission role's tab's text.
     */
    private void setMainRoleTabText() {
        view.getRoleTabs().get(MissionRole.MAIN).setText(selectedMissionType.getTitle());
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
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
}
