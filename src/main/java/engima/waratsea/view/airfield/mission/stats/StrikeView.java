package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.presenter.airfield.mission.MissionStats;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class StrikeView implements StatsView {
    private Label airbaseTitle = new Label();
    private Label distanceValue = new Label();
    private Label inRouteValue = new Label();

    private MissionStats missionStats;
    private VBox statsVBox;
    private Node statsNode;

    private ViewProps props;
    /**
     * Constructor.
     *
     * @param props The view properties.
     */
    @Inject
    public StrikeView(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the target view.
     *
     * @return The target view.
     */
    @Override
    public Node build() {
        Node airbaseStats = buildAirbaseStats();
        statsVBox = new VBox(airbaseStats);
        statsVBox.setId("target-stats-vbox");
        return statsVBox;
    }

    /**
     * Show the mission stats view.
     *
     * @param stats The mission stats.
     */
    @Override
    public void show(final MissionStats stats) {
        missionStats = stats;
        airbaseTitle.setText(missionStats.getTargetStats().getTitle());
        distanceValue.setText(missionStats.getTargetStats().getDistance());
        inRouteValue.setText(missionStats.getTargetStats().getRouteSteps());
        rebuildSuccessStats();
    }

    /**
     * Add a squadron to the current mission under construction.
     *
     * @param squadron The added squadron.
     * @param target  The added squadron's target.
     */
    @Override
    public void addSquadron(final Squadron squadron, final Target target) {
        int inRoute = Integer.parseInt(inRouteValue.getText());
        inRouteValue.setText(inRoute + squadron.getSteps().intValue() + "");
        rebuildSuccessStats();
    }

    /**
     * Remove a squadron from the mission stats.
     *
     * @param squadron The squadron removed from the mission.
     * @param target   The mission's target.
     */
    @Override
    public void removeSquadron(final Squadron squadron, final Target target) {
        int inRoute = Integer.parseInt(inRouteValue.getText());
        inRouteValue.setText(inRoute - squadron.getSteps().intValue() + "");
        rebuildSuccessStats();
    }

    /**
     * Re-buiod the mission success statistics.
     */
    private void rebuildSuccessStats() {
        List<ProbabilityStats> successStats = missionStats.getSuccessStats().get();

        statsVBox.getChildren().remove(statsNode);
        statsNode = buildAllSuccessStats(successStats);
        statsVBox.getChildren().addAll(statsNode);
    }

    /**
     * Build the airbase stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseStats() {
        Label distance = new Label("Distance:");
        Label inRoute = new Label("Steps in route:");

        GridPane gridPane = new GridPane();

        gridPane.add(distance, 0, 0);
        gridPane.add(distanceValue, 1, 0);
        gridPane.add(inRoute, 0, 1);
        gridPane.add(inRouteValue, 1, 1);
        gridPane.setId("target-step-summary-grid");

        VBox vBox = new VBox(airbaseTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }

    /**
     * Build the mission success stats.
     *
     * @param stats The success stats.
     * @return The mission success node.
     */
    private Node buildAllSuccessStats(final List<ProbabilityStats> stats) {
       List<Node> nodes = stats
               .stream()
               .map(this::buildStats)
               .collect(Collectors.toList());

       VBox vBox = new VBox();
       vBox.getChildren().addAll(nodes);
       vBox.setId("mission-stats-vbox");
       return vBox;
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
        eventHeader.setId("mission-stats-header");

        Label probHeader = new Label(probabilityStats.getProbabilityColumnTitle());
        probHeader.setMaxWidth(props.getInt("mission.grid.label.width"));
        probHeader.setMinWidth(props.getInt("mission.grid.label.width"));
        probHeader.setId("mission-stats-header");

        GridPane gridPane = new GridPane();
        gridPane.add(eventHeader, 0, 0);
        gridPane.add(probHeader, 1, 0);
        gridPane.setId("mission-stats-grid");

        Map<Integer, Integer> probability = probabilityStats.getProbability();

        int row = 1;
        for (Map.Entry<Integer, Integer> entry : probability.entrySet()) {

            Label event = new Label(entry.getKey() + "");
            event.setMaxWidth(props.getInt("mission.grid.label.width"));
            event.setMinWidth(props.getInt("mission.grid.label.width"));
            event.setId("mission-stats-cell");

            gridPane.add(event, 0, row);

            Label prob = new Label(entry.getValue() + " %");
            prob.setMaxWidth(props.getInt("mission.grid.label.width"));
            prob.setMinWidth(props.getInt("mission.grid.label.width"));
            prob.setId("mission-stats-cell");

            gridPane.add(prob, 1, row);
            row++;
        }

        VBox vBox = new VBox(title, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }
}
