package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.target.Target;
import engima.waratsea.view.ViewProps;
import engima.waratsea.viewmodel.AirMissionViewModel;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;


public class FerryView implements StatsView {
    private final Label airbaseTitle = new Label();
    private final Label distanceValue = new Label();
    private final Label inRouteValue = new Label();
    private final Label capacityValue = new Label();
    private final Label currentValue = new Label();
    private final Label targetRegionTitle = new Label();
    private final Label targetRegionMaxStepsValue = new Label();
    private final Label targetRegionCurrentStepsValue = new Label();
    private final Label targetRegionInRouteValue = new Label();
    private final Label airbaseRegionTitle = new Label();
    private final Label airbaseRegionMinStepsValue = new Label();
    private final Label airbaseRegionCurrentStepsValue = new Label();
    private final Label airbaseRegionOutRouteValue = new Label();

    private final VBox statsVBox = new VBox();
    private final ViewProps props;
    /**
     * Constructor.
     *
     * @param props The view properties.
     */
    @Inject
    public FerryView(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the target view.
     *
     * @return The target view.
     */
    @Override
    public FerryView build() {
        Node airbaseStats = buildAirbaseStats();
        Node targetRegionStats = buildTargetRegionStats();
        Node airbaseRegionStats = buildAirbaseRegionStats();

        statsVBox.getChildren().addAll(airbaseStats, targetRegionStats, airbaseRegionStats);
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
     * @return The node containing the target view.
     */
    @Override
    public Node bind(final AirMissionViewModel viewModel) {
        airbaseTitle.textProperty().bind(Bindings.createStringBinding(() -> getTargetTitle(viewModel), viewModel.getTarget()));
        distanceValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetDistance(viewModel), viewModel.getTarget()));
        inRouteValue.textProperty().bind(viewModel.getTotalStepsInRouteToTarget().asString());
        capacityValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetCapacity(viewModel), viewModel.getTarget()));
        currentValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetCurrentSteps(viewModel), viewModel.getTarget()));

        targetRegionTitle.textProperty().bind(Bindings.createStringBinding(() -> getTargetRegionTitle(viewModel), viewModel.getTarget()));
        targetRegionMaxStepsValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetRegionMaxSteps(viewModel), viewModel.getTarget()));
        targetRegionCurrentStepsValue.textProperty().bind(Bindings.createStringBinding(() -> getTargetRegionCurrentSteps(viewModel), viewModel.getTarget()));
        targetRegionInRouteValue.textProperty().bind(viewModel.getTotalStepsInRouteToTargetRegion().asString());

        airbaseRegionTitle.textProperty().bind(viewModel.getNationAirbaseViewModel().getRegionTitle());
        airbaseRegionMinStepsValue.textProperty().bind(viewModel.getNationAirbaseViewModel().getRegionMinimum());
        airbaseRegionCurrentStepsValue.textProperty().bind(viewModel.getNationAirbaseViewModel().getRegionCurrent());
        airbaseRegionOutRouteValue.textProperty().bind(viewModel.getTotalStepsLeavingRegion().asString());

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
     * Get the current target's capacity.
     *
     * @param viewModel The air mission view model.
     * @return The destination airbase's current capacity.
     */
    private String getTargetCapacity(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();

        return Optional.ofNullable(target.getValue()).map(Target::getCapacitySteps).orElse(0) + "";
    }

    /**
     * Get the current target's current number of steps.
     *
     * @param viewModel The air mission view model.
     * @return The destination airbase's current stationed number of squadron steps.
     */
    private String getTargetCurrentSteps(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();

        return Optional.ofNullable(target.getValue()).map(Target::getCurrentSteps).orElse(0) + "";
    }

    /**
     * Get the current target's region title.
     *
     * @param viewModel The air mission view model.
     * @return The destination airbase's region title.
     */
    private String getTargetRegionTitle(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        Nation nation = viewModel.getNation();

        return Optional.ofNullable(target.getValue()).map(t -> t.getRegionTitle(nation)).orElse("");
    }

    /**
     * Get the target regions maximum allowed squadron steps.
     *
     * @param viewModel The air mission view model.
     * @return The destination airbase's region's maximum allowed squadron steps.
     */
    private String getTargetRegionMaxSteps(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        Nation nation = viewModel.getNation();

        return Optional.ofNullable(target.getValue()).map(t -> t.getRegionMaxSteps(nation)).orElse(0) + "";
    }

    /**
     * Get the target regions current squadron steps.
     *
     * @param viewModel The air mission view model.
     * @return The destination airbase's region's current squadron steps.
     */
    private String getTargetRegionCurrentSteps(final AirMissionViewModel viewModel) {
        ObjectProperty<Target> target = viewModel.getTarget();
        Nation nation = viewModel.getNation();

        return Optional.ofNullable(target.getValue()).map(t -> t.getRegionCurrentSteps(nation)).orElse(0) + "";
    }

    /**
     * Build the airbase stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseStats() {
        Label distance = new Label("Distance:");
        Label capacity = new Label("Capacity in steps:");
        Label current = new Label("Stationed steps:");
        Label inRoute = new Label("Steps in route:");

        GridPane gridPane = new GridPane();

        final int row2 = 2;
        final int row3 = 3;

        gridPane.add(distance, 0, 0);
        gridPane.add(distanceValue, 1, 0);
        gridPane.add(capacity, 0, 1);
        gridPane.add(capacityValue, 1, 1);
        gridPane.add(current, 0, row2);
        gridPane.add(currentValue, 1, row2);
        gridPane.add(inRoute, 0, row3);
        gridPane.add(inRouteValue, 1, row3);

        gridPane.setId("target-step-summary-grid");

        VBox vBox = new VBox(airbaseTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }

    /**
     * Build the region stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildTargetRegionStats() {
        Label targetRegionMaxSteps = new Label("Region maximum steps:");
        Label targetRegionCurrentSteps = new Label("Region current steps:");
        Label targetRegionInRoute = new Label("Steps in route to region:");

        GridPane gridPane = new GridPane();

        final int row2 = 2;

        gridPane.add(targetRegionMaxSteps, 0, 0);
        gridPane.add(targetRegionMaxStepsValue, 1, 0);
        gridPane.add(targetRegionCurrentSteps, 0, 1);
        gridPane.add(targetRegionCurrentStepsValue, 1, 1);
        gridPane.add(targetRegionInRoute, 0, row2);
        gridPane.add(targetRegionInRouteValue, 1, row2);
        gridPane.setId("region-step-summary-grid");

        VBox vBox = new VBox(targetRegionTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }

    /**
     * Build the region stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseRegionStats() {
        Label airbaseRegionMinSteps = new Label("Region minimum steps");
        Label airbaseRegionCurrentSteps = new Label("Region current steps:");
        Label airbaseRegionOutRoute = new Label("Steps out of region:");

        GridPane gridPane = new GridPane();

        final int row2 = 2;

        gridPane.add(airbaseRegionMinSteps, 0, 0);
        gridPane.add(airbaseRegionMinStepsValue, 1, 0);
        gridPane.add(airbaseRegionCurrentSteps, 0, 1);
        gridPane.add(airbaseRegionCurrentStepsValue, 1, 1);
        gridPane.add(airbaseRegionOutRoute, 0, row2);
        gridPane.add(airbaseRegionOutRouteValue, 1, row2);
        gridPane.setId("region-step-summary-grid");

        VBox vBox = new VBox(airbaseRegionTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }
}
