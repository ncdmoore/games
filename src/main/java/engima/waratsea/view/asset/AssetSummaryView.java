package engima.waratsea.view.asset;

import com.google.inject.Singleton;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

/**
 * Represents the asset summary view at the bottom of the game screen.
 */
@Singleton
public class AssetSummaryView {

    private HBox hBox = new HBox();

    /**
     * Build the asset summary view.
     *
     * @return The node containing the asset summary view.
     */
    public Node build() {
        return hBox;
    }

    /**
     * Show the given node in the asset summary section.
     *
     * @param node The node to show.
     */
    public void show(final Node node) {
        hBox.getChildren().clear();
        hBox.getChildren().add(node);
    }

    /**
     * Hide the asset summary contents.
     */
    public void hide() {
        hBox.getChildren().clear();
    }

}
