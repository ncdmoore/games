package engima.waratsea.presenter.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.taskforce.TaskForcesNavalView;
import engima.waratsea.viewmodel.taskforce.naval.TaskForceNavalViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the task force's naval operations dialog.
 */
public class TaskForceNavalDialog {
    private static final String CSS_FILE = "taskForceNavalDetails.css";

    private List<TaskForce> taskForces;

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<TaskForcesNavalView> viewProvider;
    private final Provider<TaskForceNavalViewModel> viewModelProvider;

    private final ViewProps props;
    private Stage stage;

    private List<TaskForceNavalViewModel> viewModels;

    //CHECKSTYLE:OFF
    @Inject
    public TaskForceNavalDialog(final CssResourceProvider cssResourceProvider,
                                final Provider<DialogView> dialogProvider,
                                final Provider<TaskForcesNavalView> viewProvider,
                                final Provider<TaskForceNavalViewModel> viewModelProvider,
                                final ViewProps props) {
        //CHECKSTYLE:ON
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
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

        dialog.setWidth(props.getInt("airfield.dialog.width"));
        dialog.setHeight(props.getInt("airfield.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        TaskForcesNavalView view = viewProvider.get();

        buildViewModel();

        dialog.setContents(view.build(viewModels));

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
     * Build the view model.
     */
    private void buildViewModel() {
        viewModels = taskForces
                .stream()
                .map(taskForce -> viewModelProvider.get().setModel(taskForce))
                .collect(Collectors.toList());
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
        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        stage.close();
    }
}
