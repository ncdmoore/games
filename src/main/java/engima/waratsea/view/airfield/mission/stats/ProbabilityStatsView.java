package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Probability stats view used by missions.
 *
 * CSS styles used.
 *
 * - spacing-10
 * - spacing-75
 * - mission-stats-header
 * - mission-stats-cell
 * - mission-stats-grid
 */
public class ProbabilityStatsView {

    private final ViewProps props;
    private Pane pane = new VBox();

    /**
     * Constructor.
     *
     * @param props The view properties.
     */
    @Inject
    public ProbabilityStatsView(final ViewProps props) {
        this.props = props;
        pane.getStyleClass().add("spacing-10");
    }

    /**
     * Set the containing pane to be an HBox.
     *
     * @return This object.
     */
    public ProbabilityStatsView setHorizontal() {
        pane = new HBox();
        pane.getStyleClass().add("spacing-75");
        return this;
    }

    /**
     * Build the mission success stats.
     *
     * @param stats The success stats.
     * @return The mission success node.
     */
    public Node build(final List<ProbabilityStats> stats) {
        List<Node> nodes = stats
                .stream()
                .map(this::buildStats)
                .collect(Collectors.toList());

        pane.getChildren().clear();
        pane.getChildren().addAll(nodes);
        return pane;
    }

    /**
     * Build a single set of probability stats.
     *
     * @param probabilityStats The stats to build.
     * @return A node containing the built probability stats.
     */
    private Node buildStats(final ProbabilityStats probabilityStats) {
        Label title = new Label(probabilityStats.getTitle());

        Label eventHeader = new Label(probabilityStats.getEventColumnTitle());
        eventHeader.setMaxWidth(props.getInt("mission.grid.label.width"));
        eventHeader.setMinWidth(props.getInt("mission.grid.label.width"));
        eventHeader.getStyleClass().add("mission-stats-header");

        Label probHeader = new Label(probabilityStats.getProbabilityColumnTitle());

        if (!probabilityStats.getMetaData().isEmpty()) {
            String tooltipText = probabilityStats
                    .getMetaData()
                    .entrySet()
                    .stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("\n"));
            Tooltip tooltip = new Tooltip(tooltipText);
            probHeader.setTooltip(tooltip);
        }

        probHeader.setMaxWidth(props.getInt("mission.grid.label.width"));
        probHeader.setMinWidth(props.getInt("mission.grid.label.width"));
        probHeader.getStyleClass().add("mission-stats-header");

        GridPane gridPane = new GridPane();
        gridPane.add(eventHeader, 0, 0);
        gridPane.add(probHeader, 1, 0);
        gridPane.getStyleClass().add("mission-stats-grid");

        Map<String, Integer> probability = probabilityStats.getProbability();

        int row = 1;
        for (Map.Entry<String, Integer> entry : probability.entrySet()) {

            Label event = new Label(entry.getKey());
            event.setMaxWidth(props.getInt("mission.grid.label.width"));
            event.setMinWidth(props.getInt("mission.grid.label.width"));
            event.getStyleClass().add("mission-stats-cell");

            gridPane.add(event, 0, row);

            Label prob = new Label(entry.getValue() + " %");
            prob.setMaxWidth(props.getInt("mission.grid.label.width"));
            prob.setMinWidth(props.getInt("mission.grid.label.width"));
            prob.getStyleClass().add("mission-stats-cell");

            gridPane.add(prob, 1, row);
            row++;
        }

        return new VBox(title, gridPane);
    }
}
