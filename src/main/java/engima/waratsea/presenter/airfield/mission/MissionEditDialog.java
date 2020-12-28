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
import engima.waratsea.view.airfield.mission.MissionEditView;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MissionEditDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionEditView> viewProvider;
    private final Provider<WarnDialog> warnDialogProvider;
    private final Provider<AirMissionViewModel> missionViewModelProvider;

    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;

    private AirMissionViewModel viewModel;
    private AirMissionViewModel originalViewModel;

    @Getter private AirMissionType selectedMissionType;
    @Getter private Target selectedTarget;

    private MissionEditView view;
    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     */
    //CHECKSTYLE:OFF
    @Inject
    public MissionEditDialog(final CssResourceProvider cssResourceProvider,
                             final Provider<DialogView> dialogProvider,
                             final Provider<MissionEditView> viewProvider,
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
    //CHECKSTYLE:ON

    /**
     * Show the airfield details dialog.
     *
     * @param currentMission The air base.
     */
    public void show(final AirMissionViewModel currentMission) {
        Nation nation = currentMission.getNation();

        dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        originalViewModel = currentMission;

        viewModel = missionViewModelProvider
                .get()
                .setSquadrons(currentMission.getSquadrons())
                .setModel(currentMission.getMission())
                .setNationViewModel(currentMission.getNationAirbaseViewModel());

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(currentMission.getNationAirbaseViewModel().getTitle().getValue() + " Mission Details");

        dialog.setWidth(props.getInt("mission.dialog.width"));
        dialog.setHeight(props.getInt("mission.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.build(nation).bind(viewModel));

        registerHandlers();

        view.getMissionType().getSelectionModel().selectFirst();
        view.getTarget().getSelectionModel().selectFirst();

        selectedMissionType = view
                .getMissionType()
                .getSelectionModel()
                .getSelectedItem();

        selectedTarget = view
                .getTarget()
                .getSelectionModel()
                .getSelectedItem();

        viewModel.setMissionType(selectedMissionType);
        viewModel.setTarget(selectedTarget);

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Register callbacks.
     */
    private void registerHandlers() {
        MissionRole
                .stream()
                .forEach(this::registerListHandlers);

        viewModel.getWarning().addListener((o, ov, nv) -> warningHandler(nv));

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());
    }

    /**
     * Register the list handlers.
     *
     * @param role The mission role.
     */
    private void registerListHandlers(final MissionRole role) {
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

        view.getSquadrons().get(role).getAdd().setOnAction(event -> assignSquadron(role));
        view.getSquadrons().get(role).getRemove().setOnAction(event -> removeSquadron(role));
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
            // This allows a user to quickly add squadrons to the mission.
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
        }
    }

    /**
     * Callback for when the view model has encountered a warning.
     *
     * @param warning If true indicates a warning occurred.
     */
    private void warningHandler(final Boolean warning) {
        if (warning) {
            warnDialogProvider
                    .get()
                    .show(viewModel.getWarningText());
        }
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        viewModel.editMission();

        // Update the original mission to reflect the changes made in the edit dialog.
        originalViewModel.setModel(viewModel.getMission());

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }

}
