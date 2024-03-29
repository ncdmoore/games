package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The strike missions view stats.
 *
 * CSS used
 *
 *  - spacing-10
 *  - step-summary-grid
 */
public class StrikeView implements StatsView {
    private final Label airbaseTitle = new Label();
    private final Label distanceValue = new Label();
    private final Label etaValue = new Label();
    private final Label rttValue = new Label();
    private final Label inRouteValue = new Label();

    private VBox statsVBox;
    private Node statsNode;
    private final ProbabilityStatsView statsView;

    /**
     * Constructor.
     *
     * @param statsView The probability of success view.
     */
    @Inject
    public StrikeView(final ProbabilityStatsView statsView) {
        this.statsView = statsView;
    }

    /**
     * Build the target view.
     *
     * @return The target view.
     */
    @Override
    public StrikeView build() {
        Node airbaseStats = buildAirbaseStats();
        statsVBox = new VBox(airbaseStats);
        statsVBox.getStyleClass().add("spacing-10");
        return this;
    }

    /**
     * Get the stats view contents.
     *
     * @return The node containing the stats view.
     */
    @Override
    public Node getContents() {
        return statsVBox;
    }

    /**
     * Bind to the view model.
     *
     * @param viewModel The air mission view model.
     */
    @Override
    public Node bind(final AirMissionViewModel viewModel) {
        airbaseTitle.textProperty().bind(viewModel.getTargetTitle());
        distanceValue.textProperty().bind(viewModel.getTargetDistance());
        etaValue.textProperty().bind(viewModel.getTargetEta());
        rttValue.textProperty().bind(viewModel.getTargetRtt());
        inRouteValue.textProperty().bind(viewModel.getTotalStepsInRouteToTarget().asString());

        viewModel.getMissionStats().addListener((o, ov, nv) -> rebuildSuccessStats(nv));

        return statsVBox;
    }

    /**
     * Re-build the mission success statistics.
     *
     * @param successStats The mission success stats.
     */
    private void rebuildSuccessStats(final List<ProbabilityStats> successStats) {
        statsVBox.getChildren().remove(statsNode);
        statsNode = statsView.build(successStats);
        statsVBox.getChildren().addAll(statsNode);
    }

    /**
     * Build the airbase stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseStats() {
        Label distance = new Label("Distance:");
        Label eta = new Label("ETA (turns):");
        Label rtt = new Label("RTT (turns):");
        Label inRoute = new Label("Steps in route:");

        GridPane gridPane = new GridPane();

        int row = 0;
        gridPane.add(distance, 0, row);
        gridPane.add(distanceValue, 1, row);
        gridPane.add(eta, 0, ++row);
        gridPane.add(etaValue, 1, row);
        gridPane.add(rtt, 0, ++row);
        gridPane.add(rttValue, 1, row);
        gridPane.add(inRoute, 0, ++row);
        gridPane.add(inRouteValue, 1, row);

        gridPane.getStyleClass().add("step-summary-grid");

        return new VBox(airbaseTitle, gridPane);
    }
}
