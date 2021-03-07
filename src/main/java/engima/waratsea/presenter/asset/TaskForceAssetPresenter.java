package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.presenter.taskforce.TaskForceAirDialog;
import engima.waratsea.presenter.taskforce.TaskForceNavalDialog;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.view.asset.TaskForceAssetSummaryView;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.util.HashSet;
import java.util.Set;

@Singleton
public class TaskForceAssetPresenter {
    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<TaskForceViewModel> taskForceViewModelProvider;
    private final Provider<TaskForceAssetSummaryView> taskForceAssetSummaryViewProvider;
    private final Provider<TaskForceAirDialog> taskForceAirDialogProvider;
    private final Provider<TaskForceNavalDialog> taskForceNavalDialogProvider;


    private final Set<AssetId> hideAssets = new HashSet<>();

    @Inject
    public TaskForceAssetPresenter(final Provider<AssetSummaryView> assetSummaryViewProvider,
                                   final Provider<TaskForceViewModel> taskForceViewModelProvider,
                                   final Provider<TaskForceAssetSummaryView> taskForceAssetSummaryViewProvider,
                                   final Provider<TaskForceAirDialog> taskForceAirDialogProvider,
                                   final Provider<TaskForceNavalDialog> taskForceNavalDialogProvider) {
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.taskForceViewModelProvider = taskForceViewModelProvider;
        this.taskForceAssetSummaryViewProvider = taskForceAssetSummaryViewProvider;
        this.taskForceAirDialogProvider = taskForceAirDialogProvider;
        this.taskForceNavalDialogProvider = taskForceNavalDialogProvider;
    }

    /**
     * Add a task force to the asset summary.
     *
     * @param taskForce The task force to add.
     */
    public void addTaskForceToAssetSummary(final TaskForce taskForce) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());

        TaskForceViewModel viewModel = taskForceViewModelProvider
                .get()
                .setModel(taskForce);

        TaskForceAssetSummaryView assetView = taskForceAssetSummaryViewProvider.get();
        assetView.build(viewModel);
        assetSummaryViewProvider.get().show(assetId, assetView);
        registerCallbacks(assetView);
    }

    /**
     * Remove a task force from the asset summary.
     *
     * @param taskForce The task force to remove.
     */
    public void removeTaskForceFromAssetSummary(final TaskForce taskForce) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());
        assetSummaryViewProvider.get().hide(assetId);
    }

    /**
     * Get the view model for the given task force. This is the view model
     * used by the asset summary view.
     *
     * @param taskForce The task force for which the view model is retrieved.
     * @return The task force view model retrieved from the task force asset view.
     */
    public TaskForceViewModel getViewModel(final TaskForce taskForce) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());
        AssetSummaryView assetManager = assetSummaryViewProvider.get();

        TaskForceAssetSummaryView taskForceAssetView = (TaskForceAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseGet(() -> addTaskForceToAssetView(taskForce));

        assetManager.show(assetId, taskForceAssetView);

        return taskForceAssetView.getViewModel();
    }

    /**
     * Hide the task force's asset summary.
     *
     * @param taskForce The task force whose asset should be hidden.
     * @param reset   Indicates if the asset's view model should be reset.
     */
    public void hide(final TaskForce taskForce, final boolean reset) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());

        if (hideAssets.contains(assetId)) {
            assetSummaryViewProvider.get().hide(assetId);
            hideAssets.remove(assetId);
        } else if (reset) {
            reset(taskForce);
        }
    }

    /**
     * Any changes that were not saved to the task force need to be reflected in the asset summary view of
     * the task force. Thus, the task force's view model is reset to the data stored in the task force's model.
     * This way the task force's asset summary contains the current data from the model. This is only needed
     * when the task force asset summary survives the dialog's cancel button, i.e., when the dialog does
     * not control the display of the task force asset summary.
     *
     * @param taskForce The task force that was not saved.
     */
    private void reset(final TaskForce taskForce) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());

        TaskForceViewModel viewModel = taskForceViewModelProvider
                .get()
                .setModel(taskForce);

        AssetSummaryView assetManager = assetSummaryViewProvider.get();

        TaskForceAssetSummaryView assetView = (TaskForceAssetSummaryView) assetManager
                .getAsset(assetId)
                .orElseThrow();

        assetView.reset(viewModel);   // reset the task force's asset summary's view of the task force.
    }

    /**
     * Register callbacks for task force asset presenter.
     *
     * @param assetView The task force asset summary view.
     */
    private void registerCallbacks(final TaskForceAssetSummaryView assetView) {
        assetView
                .getAirOperations()
                .setOnAction(this::taskForceManageAirOpts);

        assetView
                .getNavalOperations()
                .setOnAction(this::taskForceManageNavalOpts);
    }

    private TaskForceAssetSummaryView addTaskForceToAssetView(final TaskForce taskForce) {
        AssetId assetId = new AssetId(AssetType.TASK_FORCE, taskForce.getTitle());

        TaskForceViewModel viewModel = taskForceViewModelProvider
                .get()
                .setModel(taskForce);

        TaskForceAssetSummaryView assetView = taskForceAssetSummaryViewProvider.get();
        assetView.build(viewModel);

        hideAssets.add(assetId);

        return assetView;
    }

    /**
     * Callback for manage airfield mission button.
     *
     * @param event The button click event.
     */
    private void taskForceManageAirOpts(final ActionEvent event) {
        Button button = (Button) event.getSource();
        TaskForce taskForce = (TaskForce) button.getUserData();

        taskForceAirDialogProvider
                .get()
                .show(taskForce);
    }

    /**
     * Callback for manage airfield mission button.
     *
     * @param event The button click event.
     */
    private void taskForceManageNavalOpts(final ActionEvent event) {
        Button button = (Button) event.getSource();
        TaskForce taskForce = (TaskForce) button.getUserData();

        taskForceNavalDialogProvider
                .get()
                .show(taskForce);
    }
}
