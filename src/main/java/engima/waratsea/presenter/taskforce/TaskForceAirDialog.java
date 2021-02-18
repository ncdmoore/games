package engima.waratsea.presenter.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.map.MainMapView;
import engima.waratsea.view.taskforce.TaskForcesAirView;
import engima.waratsea.viewmodel.taskforce.air.TaskForcesAirViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * Represents the Task force's air operations dialog.
 */
public class TaskForceAirDialog {
    private static final String CSS_FILE = "taskForceAirDetails.css";

    private List<TaskForce> taskForces;

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<MainMapView> mapViewProvider;
    private final Provider<TaskForcesAirView> viewProvider;
    private final Provider<TaskForcesAirViewModel> viewModelProvider;

    private final ViewProps props;
    private Stage stage;

    private MainMapView mapView;

    private TaskForcesAirViewModel viewModel;

    //CHECKSTYLE:OFF
    @Inject
    public TaskForceAirDialog(final CssResourceProvider cssResourceProvider,
                              final Provider<DialogView> dialogProvider,
                              final Provider<MainMapView> mapViewProvider,
                              final Provider<TaskForcesAirView> viewProvider,
                              final Provider<TaskForcesAirViewModel> viewModelProvider,
                              final ViewProps props) {
        //CHECKSTYLE:ON
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.mapViewProvider = mapViewProvider;
        this.viewProvider = viewProvider;
        this.viewModelProvider = viewModelProvider;
        this.props = props;
    }

    /**
     * Show the task force details dialog.
     *
     * @param forces The task forces
     */
    public void show(final List<TaskForce> forces) {
        taskForces = forces;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(determineTitle());

        DialogView dialog = dialogProvider.get();

        dialog.setWidth(props.getInt("taskforce.air.dialog.width"));
        dialog.setHeight(props.getInt("taskforce.air.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        mapView = mapViewProvider.get();

        TaskForcesAirView view = viewProvider.get();

        viewModel = viewModelProvider
                .get()
                .setModel(taskForces);

        dialog.setContents(view.build(viewModel));

        registerHandlers(dialog);

        dialog.show(stage);

        // No code can go here. The dialog blocks until closed.

    }

    /**
     * Determine the dialog title.
     *
     * @return The dialog title.
     */
    private String determineTitle() {
        return taskForces.size() > 1 ? "Multiple Task Force Details" : taskForces.get(0).getTitle() + " Details";
    }

    /**
     * Register callback handlers.
     *
     * @param dialog This dialog's view.
     */
    private void registerHandlers(final DialogView dialog) {
        dialog.getCancelButton().setOnAction(event -> cancel());
        dialog.getOkButton().setOnAction(event -> ok());
    }

    /**
     * Call back for the ok button.
     */
    private void ok() {
        viewModel.save();

        mapView.toggleTaskForceMarkers(taskForces.get(0));   // There must be at least one task force.

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }
}
