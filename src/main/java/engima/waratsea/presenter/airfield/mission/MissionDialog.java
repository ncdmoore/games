package engima.waratsea.presenter.airfield.mission;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogOkOnlyView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.MissionDetailsView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Controls the mission details dialog shown when a mission arrow is clicked on the main game map.
 */
@Slf4j
public class MissionDialog {
    private static final String CSS_FILE = "missionDetails.css";

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogOkOnlyView> dialogProvider;
    private final Provider<MissionDetailsView> viewProvider;
    private final ViewProps props;

    private Stage stage;

    /**
     * Constructor called by guice.
     *
     * @param cssResourceProvider Provides CSS file.
     * @param dialogProvider Provides the dialog view.
     * @param viewProvider Provides the patrol view.
     * @param props The view property.
     */
    @Inject
    public MissionDialog(final CssResourceProvider cssResourceProvider,
                         final Provider<DialogOkOnlyView> dialogProvider,
                         final Provider<MissionDetailsView> viewProvider,
                         final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.props = props;
    }

    /**
     * Show the airfield's mission details dialog.
     *
     * @param missions The missions for the selected arrow.
     */
    public void show(final List<AirMission> missions) {
        Airbase airbase = missions.get(0).getAirbase();

        DialogOkOnlyView dialog = dialogProvider.get();     // The dialog view that contains the airfield details view.
        MissionDetailsView view = viewProvider.get();

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(airbase.getTitle() + " " +  airbase.getAirbaseType().getTitle() + " Patrol Details");

        dialog.setWidth(props.getInt("mission.details.dialog.width"));
        dialog.setHeight(props.getInt("mission.details.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));
        dialog.setContents(view.show(missions));

        dialog.getOkButton().setOnAction(event -> ok());

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.
    }

    /**
     * Close this dialog.
     */
    private void ok() {
        stage.close();
    }
}
