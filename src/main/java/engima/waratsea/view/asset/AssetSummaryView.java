package engima.waratsea.view.asset;

import com.google.inject.Singleton;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the asset summary view at the bottom of the game screen.
 * This class is essentially a manager of the asset types that are displayed in the asset summary view area at the
 * bottom of the game screen. This class adds asset views to this area and removes assets from this area.
 */
@Singleton
public class AssetSummaryView {
    private final Map<AssetId, Tab> tabMap = new HashMap<>();
    private final Map<AssetId, AssetView> viewMap = new HashMap<>();
    private final TabPane tabPane = new TabPane();

    /**
     * Build the asset summary view.
     *
     * @return The node containing the asset summary view.
     */
    public Node build() {
        tabMap.clear();
        tabPane.getTabs().clear();
        return tabPane;
    }

    /**
     * Show the given node in the asset summary section.
     *
     * @param assetId The asset's Id.
     * @param assetView The asset to show.
     */
    public void show(final AssetId assetId, final AssetView assetView) {
        boolean alreadyOnTabPane = tabMap.containsKey(assetId);

        if (!alreadyOnTabPane) {
            addTab(assetId, assetView);
        }

        selectTab(assetId);
    }

    /**
     * Get the asset view of the given asset Id.
     *
     * @param assetId The asset's Id.
     * @return The corresponding asset view of the given asset Id.
     */
    public Optional<AssetView> getAsset(final AssetId assetId) {
        return Optional.ofNullable(viewMap.get(assetId));
    }

    /**
     * Hide the asset summary contents.
     *
     *  @param assetId The Id of the asset to hide.
     */
    public void hide(final AssetId assetId) {
        if (tabMap.containsKey(assetId)) {
            tabPane.getTabs().remove(tabMap.get(assetId));
            tabMap.remove(assetId);
            viewMap.remove(assetId);
        }
    }

    /**
     * Select the tab specified by the key.
     *
     * @param assetId Specifies which tab to select.
     */
    private void selectTab(final AssetId assetId) {
        tabPane
                .getSelectionModel()
                .select(tabMap.get(assetId));
    }

    /**
     * Add a tab to the asset summary view.
     *
     * @param assetId Identifies the asset.
     * @param assetView The contents of the tab.
     */
    private void addTab(final AssetId assetId, final AssetView assetView) {
        Tab tab = new Tab();
        tab.setText(assetId.getName());
        tab.setContent(assetView.getNode());
        tabPane.getTabs().add(tab);
        tabMap.put(assetId, tab);
        viewMap.put(assetId, assetView);
    }
}
