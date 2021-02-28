package engima.waratsea.presenter.asset;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.game.AssetType;
import engima.waratsea.model.taskForce.TaskForce;
import engima.waratsea.view.asset.AssetId;
import engima.waratsea.view.asset.AssetSummaryView;
import engima.waratsea.view.asset.TaskForceAssetSummaryView;
import engima.waratsea.viewmodel.taskforce.TaskForceViewModel;

@Singleton
public class TaskForceAssetPresenter {
    private final Provider<AssetSummaryView> assetSummaryViewProvider;
    private final Provider<TaskForceViewModel> taskForceViewModelProvider;
    private final Provider<TaskForceAssetSummaryView> taskForceAssetSummaryViewProvider;

    @Inject
    public TaskForceAssetPresenter(final Provider<AssetSummaryView> assetSummaryViewProvider,
                                   final Provider<TaskForceViewModel> taskForceViewModelProvider,
                                   final Provider<TaskForceAssetSummaryView> taskForceAssetSummaryViewProvider) {
        this.assetSummaryViewProvider = assetSummaryViewProvider;
        this.taskForceViewModelProvider = taskForceViewModelProvider;
        this.taskForceAssetSummaryViewProvider = taskForceAssetSummaryViewProvider;
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
}
