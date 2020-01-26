package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionAddDetailsView;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MissionAddDetailsDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionAddDetailsView> viewProvider;
    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;
    private AirfieldDetailsDialog airfieldDialog;
    private MissionAddDetailsView view;

    private final Game game;
    private final MissionDAO missionDAO;
    private Airbase airbase;
    private Nation nation;

    @Getter
    private Mission mission;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param missionDAO Adds missions to air bases.
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     */

    @Inject
    public MissionAddDetailsDialog(final Game game,
                                   final MissionDAO missionDAO,
                                   final CssResourceProvider cssResourceProvider,
                                   final Provider<DialogView> dialogProvider,
                                   final Provider<MissionAddDetailsView> viewProvider,
                                   final ViewProps props) {
        this.game = game;
        this.missionDAO = missionDAO;
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

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
        return this;
    }

    /**
     * Show the airfield details dialog.
     *
     * @param currentAirbase The air base.
     */
    public void show(final Airbase currentAirbase) {
        airbase = currentAirbase;

        dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " Mission Details");

        dialog.setWidth(props.getInt("mission.dialog.width"));
        dialog.setHeight(props.getInt("mission.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(getValidMissionTypes()));

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

        view
                .getMissionList()
                .getAdd()
                .setOnAction(this::addSquadron);

        view
                .getMissionList()
                .getRemove()
                .setOnAction(this::removeSquadron);
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

        //Filter out this airbase from the list of targets for FERRY missions.
        //No need in ferry aircraft to the same air base. That would accomplish nothing.
        targets = missionType == MissionType.FERRY ? filterThisAirbase(targets) : targets;

        view.getTarget().getItems().clear();
        view.getMissionList().clearAll();

        view.getTarget().getItems().addAll(targets);
        view.getTarget().getSelectionModel().selectFirst();

        view.getMissionList().setAvailableTitle(missionType + " Available");
        view.getMissionList().setAssignedTitle(missionType + " Assigned");
    }

    /**
     * Callback when a target has been selected.
     *
     * @param selectedTarget The selected target.
     */
    private void targetSelected(final Target selectedTarget) {
        view.getMissionList().clearAll();
        view.hideError();

        Optional.ofNullable(selectedTarget).ifPresent(target -> {

            List<Squadron> availableSquadrons = airfieldDialog
                    .getReady(nation);

            if (availableSquadrons.isEmpty()) {
                view.showError("No ready squadrons.");
                return;
            }

            availableSquadrons = availableSquadrons.stream()
                    .filter(target::inRange)
                    .collect(Collectors.toList());

            if (availableSquadrons.isEmpty()) {
                view.showError("No squadrons in range.");
                return;
            }

            view.getMissionList().addAllToAvailable(availableSquadrons);
        });
    }

    /**
     * Add a squadron to the mission.
     *
     * @param event The button action event.
     */
    private void addSquadron(final ActionEvent event) {
        view.assign();
        dialog.getOkButton().setDisable(false);
    }

    /**
     * Remove a squadron from the mission.
     *
     * @param event The button action event.
     */
    private void removeSquadron(final ActionEvent event) {
        view.remove();
        if (view.getMissionList().getAssigned().getItems().isEmpty()) {
            dialog.getOkButton().setDisable(true);
        }
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        MissionType missionType = view.getMissionType().getSelectionModel().getSelectedItem();
        Target target = view.getTarget().getSelectionModel().getSelectedItem();
        List<Squadron> squadrons = view.getMissionList().getAssigned().getItems();

        MissionData data = new MissionData();
        data.setNation(nation);
        data.setType(missionType);
        data.setTarget(target.getName());
        data.setAirbase(airbase);
        data.setSquadrons(squadrons
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
}
