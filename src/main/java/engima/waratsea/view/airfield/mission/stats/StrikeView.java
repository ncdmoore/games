package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.AirMissionViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class StrikeView implements StatsView {
    private Label airbaseTitle = new Label();
    private Label distanceValue = new Label();
    private Label inRouteValue = new Label();

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
    public StrikeView build() {
        Node airbaseStats = buildAirbaseStats();
        statsVBox = new VBox(airbaseStats);
        statsVBox.setId("target-stats-vbox");
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
        airbaseTitle.textProperty().bind(Bindings.createStringBinding(() -> getTargetName(viewModel), viewModel.getTarget()));
        distanceValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetDistance(viewModel), viewModel.getTarget()));
        inRouteValue.textProperty().bind(viewModel.getTotalStepsInRouteToTarget().asString());

        viewModel.getMissionStats().addListener((o, ov, nv) -> rebuildSuccessStats(nv));

        return statsVBox;
    }

    /**
     * Get the current target's name.
     *
     * @param viewModel The air mission view model.
     * @return The name of the destination airbase.
     */
    private String getTargetName(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        return Optional.ofNullable(target.getValue()).map(Target::getName).orElse("");
    }

    /**
     * Get the current target's distance.
     *
     * @param viewModel The air mission view model.
     * @return The distance to the destination airbase.
     */
    private String getTargetDistance(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        Airbase airbase = viewModel.getAirbase();

        return Optional.ofNullable(target.getValue()).map(t -> t.getDistance(airbase)).orElse(0) + "";
    }

    /**
     * Re-build the mission success statistics.
     *
     * @param successStats The mission success stats.
     */
    private void rebuildSuccessStats(final List<ProbabilityStats> successStats) {

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
