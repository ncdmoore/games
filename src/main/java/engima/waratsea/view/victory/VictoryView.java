package engima.waratsea.view.victory;

import javafx.scene.Node;
import javafx.scene.control.TabPane;

public class VictoryView {

    private final TabPane victoryTabPane = new TabPane();

    /**
     * Build the victory conditions view.
     *
     * @return The node containing the victory conditions.
     */
    public Node build() {
        victoryTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        return victoryTabPane;
    }
}
