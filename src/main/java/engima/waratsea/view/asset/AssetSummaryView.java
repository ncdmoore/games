package engima.waratsea.view.asset;

import com.google.inject.Singleton;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the asset summary view at the bottom of the game screen.
 * This class is essentially a manager of the asset types that are displayed in the asset summary view area at the
 * bottom of the game screen. This class add's asset views to this area and removes assets from this area.
 */
@Singleton
public class AssetSummaryView {

    private Map<String, Tab> tabMap = new HashMap<>();
    private Map<String, AssetView> viewMap = new HashMap<>();
    private TabPane tabPane = new TabPane();

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
        String key = assetId.getKey();

        boolean alreadyOnTabPane = tabMap.containsKey(key);

        if (!alreadyOnTabPane) {
            addTab(assetId, assetView);
        }

        selectTab(key);
    }

    /**
     * Get the asset view of the given asset Id.
     *
     * @param assetId The asset's Id.
     * @return The corresponding asset view of the given asset Id.
     */
    public AssetView getAsset(final AssetId assetId) {
        String key = assetId.getKey();
        return viewMap.get(key);
    }

    /**
     * Hide the asset summary contents.
     *
     *  @param assetId The Id of asset to hide.
     */
    public void hide(final AssetId assetId) {
        String key = assetId.getKey();

        if (tabMap.containsKey(key)) {
            tabPane.getTabs().remove(tabMap.get(key));
            tabMap.remove(key);
        }
    }

    /**
     * Select the tab specified by the key.
     *
     * @param key Specifies which tab to select.
     */
    private void selectTab(final String key) {
        tabPane
                .getSelectionModel()
                .select(tabMap.get(key));
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
        tabMap.put(assetId.getKey(), tab);
        viewMap.put(assetId.getKey(), assetView);
    }
}
