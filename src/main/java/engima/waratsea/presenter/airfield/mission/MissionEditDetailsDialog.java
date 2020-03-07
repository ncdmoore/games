package engima.waratsea.presenter.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.presenter.airfield.AirfieldDetailsDialog;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionEditDetailsView;
import engima.waratsea.view.airfield.mission.MissionView;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The presenter for the mission edit dialog.
 */
@Slf4j
public class MissionEditDetailsDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final ImageResourceProvider imageResourceProvider;
    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionEditDetailsView> viewProvider;
    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;
    private AirfieldDetailsDialog airfieldDialog;

    private final MissionDAO missionDAO;
    private Airbase airbase;
    private Nation nation;

    @Getter
    @Setter
    private AirMission mission;

    private MissionType selectedMissionType;

    private MissionEditDetailsView view;

    private MissionDetails missionDetails;

    /**
     * Constructor called by guice.
     *
     * @param missionDAO Adds missions to air bases.
     * @param imageResourceProvider Provides images.
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     * @param missionDetails The mission details helper.
     */
    //CHECKSTYLE:OFF
    @Inject
    public MissionEditDetailsDialog(final MissionDAO missionDAO,
                                    final ImageResourceProvider imageResourceProvider,
                                    final CssResourceProvider cssResourceProvider,
                                    final Provider<DialogView> dialogProvider,
                                    final Provider<MissionEditDetailsView> viewProvider,
                                    final ViewProps props,
                                    final MissionDetails missionDetails) {
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
    public MissionEditDetailsDialog setParentDialog(final AirfieldDetailsDialog airfieldDetailsDialog) {
        airfieldDialog = airfieldDetailsDialog;
        return this;
    }

    /**
     * Set the nation.
     *
     * @param currentNation The nation: BRITISH, ITALIAN, etc.
     * @return This mission details dialog.
     */
    public MissionEditDetailsDialog setNation(final Nation currentNation) {
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
        dialog.setContents(view.show(nation, mission));

        registerHandlers();

        view.getMissionType().getSelectionModel().selectFirst();
        view.getTarget().getSelectionModel().selectFirst();
        view.getMissionList().clearAll();

        setAvailableSquadrons();
        setAssignedSquadrons();

        selectedMissionType = view
                .getMissionType()
                .getSelectionModel()
                .getSelectedItem();

        Target selectedTarget = view.getTarget()
                .getSelectionModel()
                .getSelectedItem();

        missionDetails.setSelectedTarget(selectedTarget);

        view.getImageView().setImage(imageResourceProvider.getImage(nation.toString() + selectedMissionType.toString() + ".png"));

        missionDetails.updateTargetView(mission, selectedMissionType);

        view.getMissionList().setAvailableTitle(selectedMissionType + " Available");
        view.getMissionList().setAssignedTitle(selectedMissionType + " Assigned");

        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Initialize the available squadrons.
     **/
    private void setAvailableSquadrons() {
        Target target = mission.getTarget();

        List<Squadron> availableSquadrons = airfieldDialog
                .getReady(nation)
                .stream()
                .filter(target::inRange)
                .collect(Collectors.toList());

        view.getMissionList().addAllToAvailable(availableSquadrons);
    }

    /**
     * Initialize the assigned squadrons.
     */
    private void setAssignedSquadrons() {
        view.getMissionList().addAllToAssigned(mission.getSquadrons());
    }

    /**
     * Register the handlers for the mission dialog actions.
     */
    private void registerHandlers() {
        view.getMissionList()
                .getAssigned()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> assignedSquadronSelected(newValue));

        view.getMissionList()
                .getAvailable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((v, oldValue, newValue) -> availableSquadronSelected(newValue));

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
     * An available squadron has been selected.
     *
     * @param squadron The available squadron.
     */
    private void availableSquadronSelected(final Squadron squadron) {
        Optional
                .ofNullable(squadron)
                .ifPresent(s -> {
                    view.getSquadronSummaryView().setSelectedSquadron(s);
                    view.getMissionList().getAssigned().getSelectionModel().clearSelection();
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
                    view.getSquadronSummaryView().setSelectedSquadron(s);
                    view.getMissionList().getAvailable().getSelectionModel().clearSelection();
                });
    }

    /**
     * Add a squadron to the mission.
     *
     * @param event The button action event.
     */
    private void addSquadron(final ActionEvent event) {
        missionDetails.getSelectedAvailableSquadron().ifPresent(squadron -> {
            if (missionDetails.mayAddSquadronToMission(mission, selectedMissionType, squadron)) {
                missionDetails.addSquadron(squadron);
            }
        });
    }

    /**
     * Remove a squadron from the mission.
     *
     * @param event The button action event.
     */
    private void removeSquadron(final ActionEvent event) {
        missionDetails.getSelectedAssignedSquadron().ifPresent(squadron -> {
            missionDetails.removeSquadron();
            if (view.getMissionList().getAssigned().getItems().isEmpty()) {
                dialog.getOkButton().setDisable(true);
            }
        });

    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        MissionType missionType = mission.getType();
        Target target = mission.getTarget();
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
}
