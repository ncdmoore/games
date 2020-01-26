package engima.waratsea.presenter.airfield;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.base.airfield.mission.MissionDAO;
import engima.waratsea.model.base.airfield.mission.MissionType;
import engima.waratsea.model.base.airfield.mission.data.MissionData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionEditDetailsView;
import javafx.event.ActionEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class MissionEditDetailsDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MissionEditDetailsView> viewProvider;
    private final ViewProps props;

    private DialogView dialog;
    private Stage stage;
    private AirfieldDetailsDialog airfieldDialog;
    private MissionEditDetailsView view;

    private final MissionDAO missionDAO;
    private Airbase airbase;
    private Nation nation;

    @Getter
    @Setter
    private Mission mission;

    /**
     * Constructor called by guice.
     *
     * @param missionDAO Adds missions to air bases.
     * @param cssResourceProvider Provides the css file.
     * @param dialogProvider Provides the view for this dialog.
     * @param viewProvider Provides the view contents for this dialog.
     * @param props The view properties.
     */

    @Inject
    public MissionEditDetailsDialog(final MissionDAO missionDAO,
                                    final CssResourceProvider cssResourceProvider,
                                    final Provider<DialogView> dialogProvider,
                                    final Provider<MissionEditDetailsView> viewProvider,
                                    final ViewProps props) {
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
        dialog.setContents(view.show(mission));

        registerHandlers();

        view.getMissionType().getSelectionModel().selectFirst();
        view.getTarget().getSelectionModel().selectFirst();
        view.getMissionList().clearAll();

        setAvailableSquadrons();
        setAssignedSquadrons();

        MissionType missionType = view.getMissionType().getSelectionModel().getSelectedItem();

        view.getMissionList().setAvailableTitle(missionType + " Available");
        view.getMissionList().setAssignedTitle(missionType + " Assigned");

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
