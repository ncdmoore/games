package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.airfield.AirMissionViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;


public class StrikeView implements StatsView {
    private final Label airbaseTitle = new Label();
    private final Label distanceValue = new Label();
    private final Label inRouteValue = new Label();

    private VBox statsVBox;
    private Node statsNode;
    private final ProbabilityStatsView statsView;

    private final ViewProps props;
    /**
     * Constructor.
     *
     * @param statsView The probability of success view.
     * @param props The view properties.
     */
    @Inject
    public StrikeView(final ProbabilityStatsView statsView,
                      final ViewProps props) {
        this.statsView = statsView;
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
        airbaseTitle.textProperty().bind(Bindings.createStringBinding(() -> getTargetTitle(viewModel), viewModel.getTarget()));
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
    private String getTargetTitle(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        return Optional.ofNullable(target.getValue()).map(Target::getTitle).orElse("");
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
}
