package engima.waratsea.view.asset;

import com.google.inject.Singleton;
import engima.waratsea.model.game.AssetType;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the asset summary view at the bottom of the game screen.
 */
@Singleton
public class AssetSummaryView {

    private Map<String, Tab> map = new HashMap<>();
    private TabPane tabPane = new TabPane();

    /**
     * Build the asset summary view.
     *
     * @return The node containing the asset summary view.
     */
    public Node build() {
        map.clear();
        tabPane.getTabs().clear();
        return tabPane;
    }

    /**
     * Show the given node in the asset summary section.
     *
     * @param assetType The type of asset.
     * @param name The name of the asset.
     * @param node The node to show.
     * @return True if the asset is newly added. False otherwise.
     */
    public boolean show(final AssetType assetType, final String name, final Node node) {
        String key = assetType.toString() + name;

        boolean alreadyOnTabPane = map.containsKey(key);

        if (alreadyOnTabPane) {
            selectTab(key);
            return false;
        } else {
            addTab(key, name, node);
            selectTab(key);
            return true;
        }
    }

    /**
     * Hide the asset summary contents.
     *
     * @param assetType The type of asset to hide.
     * @param name The name of the asset to hide.
     */
    public void hide(final AssetType assetType, final String name) {
        String key = assetType.toString() + name;

        if (map.containsKey(key)) {
            tabPane.getTabs().remove(map.get(key));
            map.remove(key);
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
                .select(map.get(key));
    }

    /**
     * Add a tab to the asset summary view.
     *
     * @param key Identifies the tab.
     * @param name The title of the tab.
     * @param node The contents of the tab.
     */
    private void addTab(final String key, final String name, final Node node) {
        Tab tab = new Tab();
        tab.setText(name);
        tab.setContent(node);
        tabPane.getTabs().add(tab);
        map.put(key, tab);
    }
}
