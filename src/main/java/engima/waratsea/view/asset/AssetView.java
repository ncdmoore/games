package engima.waratsea.view.asset;

import javafx.scene.Node;

public interface AssetView {
    /**
     * Get the main content node of the asset view. This is the top most node.
     *
     * @return The main content node of this asset view.
     */
    Node getNode();
}
