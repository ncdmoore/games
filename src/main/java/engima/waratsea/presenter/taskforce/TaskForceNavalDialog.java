package engima.waratsea.presenter.taskforce;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.asset.AssetPresenter;
import engima.waratsea.utility.CssResourceProvider;
import engima.waratsea.view.DialogView;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.taskforce.TaskForceNavalView;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Represents the task force's naval operations dialog.
 */
public class TaskForceNavalDialog {
    private static final String CSS_FILE = "taskForceNavalDetails.css";

    private TaskForce taskForce;

    private final CssResourceProvider cssResourceProvider;
    private final Provider<DialogView> dialogProvider;
    private final Provider<TaskForceNavalView> viewProvider;
    private final Provider<TaskForceViewModel> viewModelProvider;
    private final AssetPresenter assetPresenter;

    private final ViewProps props;
    private Stage stage;

    private TaskForceViewModel viewModel;

    @Inject
    public TaskForceNavalDialog(final CssResourceProvider cssResourceProvider,
                                final Provider<DialogView> dialogProvider,
                                final Provider<TaskForceNavalView> viewProvider,
                                final Provider<TaskForceViewModel> viewModelProvider,
                                final AssetPresenter assetPresenter,
                                final ViewProps props) {
        this.cssResourceProvider = cssResourceProvider;
        this.dialogProvider = dialogProvider;
        this.viewProvider = viewProvider;
        this.viewModelProvider = viewModelProvider;
        this.assetPresenter = assetPresenter;
        this.props = props;
    }

    /**
     * Show the task force details dialog.
     *
     * @param force The task force
     */
    public void show(final TaskForce force) {
        taskForce = force;

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(determineTitle());

        DialogView dialog = dialogProvider.get();

        dialog.setWidth(props.getInt("taskforce.naval.dialog.width"));
        dialog.setHeight(props.getInt("taskforce.naval.dialog.height"));
        dialog.setCss(cssResourceProvider.get(CSS_FILE));

        TaskForceNavalView view = viewProvider.get();

        viewModel = assetPresenter
                .getTaskForceAssetPresenter()
                .getViewModel(taskForce);

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
        return taskForce.getTitle() + " Details";
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
        assetPresenter
                .getTaskForceAssetPresenter()
                .hide(taskForce, false);

        stage.close();
    }

    /**
     * Call back for the cancel button.
     */
    private void cancel() {
        assetPresenter
                .getTaskForceAssetPresenter()
                .hide(taskForce, true);

        stage.close();
    }
}
