package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetFriendlyAirfield;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.Optional;

/**
 * Represents the mission's target view details.
 */
public class TargetView {

    @Getter
    private Label airbaseTitle = new Label();

    private Label distance = new Label("Distance:");

    @Getter
    private Label distanceValue = new Label();

    private Label inRoute = new Label("Steps in route:");

    @Getter
    private Label inRouteValue = new Label();

    private Label capacity = new Label("Capacity in steps:");

    @Getter
    private Label capacityValue = new Label();

    private Label current = new Label("Stationed steps:");

    @Getter
    private Label currentValue = new Label();

    @Getter
    private Label targetRegionTitle = new Label();

    private Label targetRegionMaxSteps = new Label("Region maximum steps:");

    @Getter
    private Label targetRegionMaxStepsValue = new Label();

    private Label targetRegionCurrentSteps = new Label("Region current steps:");

    @Getter
    private Label targetRegionCurrentStepsValue = new Label();

    private Label targetRegionInRoute = new Label("Steps in route to region:");

    @Getter
    private Label targetRegionInRouteValue = new Label();

    @Getter
    private Label airbaseRegionTitle = new Label();

    private Label airbaseRegionMinSteps = new Label("Region minimum steps");

    @Getter
    private Label airbaseRegionMinStepsValue = new Label();

    private Label airbaseRegionCurrentSteps = new Label("Region current steps:");

    @Getter
    private Label airbaseRegionCurrentStepsValue = new Label();

    private Label airbaseRegionOutRoute = new Label("Steps out of region:");

    @Getter
    private Label airbaseRegionOurRouteValue = new Label();

    private Node targetRegionStats;
    private Node airbaseRegionStats;
    private ViewProps props;

    /**
     * Constructor.
     *
     * @param props The view properties.
     */
    @Inject
    public TargetView(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the target view.
     *
     * @param base The airbase that has the mission with this as a target.
     * @return The target view.
     */
    public Node build(final Airbase base) {

        Node airbaseStats = buildAirbaseStats();
        targetRegionStats = buildTargetRegionStats();
        airbaseRegionStats = buildAirbaseRegionStats();

        VBox vBox = new VBox(airbaseStats, targetRegionStats, airbaseRegionStats);
        vBox.setId("target-stats-vbox");
        return vBox;
    }

    /**
     * Show the target view.
     *
     * @param target The airbase's mission's target.
     */
    public void show(final Target target) {
        Optional.ofNullable(target).ifPresent(t -> {

            if (target.getClass() == TargetFriendlyAirfield.class) {
                capacity.setVisible(true);
                capacityValue.setVisible(true);
                current.setVisible(true);
                currentValue.setVisible(true);
                targetRegionStats.setVisible(true);
                airbaseRegionStats.setVisible(true);
            } else {
                capacity.setVisible(false);
                capacityValue.setVisible(false);
                current.setVisible(false);
                currentValue.setVisible(false);
                targetRegionStats.setVisible(false);
                airbaseRegionStats.setVisible(false);
            }
        });
    }

    /**
     * Build the airbase stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseStats() {
        GridPane gridPane = new GridPane();

        final int row2 = 2;
        final int row3 = 3;

        gridPane.add(distance, 0, 0);
        gridPane.add(distanceValue, 1, 0);
        gridPane.add(inRoute, 0, 1);
        gridPane.add(inRouteValue, 1, 1);
        gridPane.add(capacity, 0, row2);
        gridPane.add(capacityValue, 1, row2);
        gridPane.add(current, 0, row3);
        gridPane.add(currentValue, 1, row3);
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
        GridPane gridPane = new GridPane();

        final int row2 = 2;

        gridPane.add(airbaseRegionMinSteps, 0, 0);
        gridPane.add(airbaseRegionMinStepsValue, 1, 0);
        gridPane.add(airbaseRegionCurrentSteps, 0, 1);
        gridPane.add(airbaseRegionCurrentStepsValue, 1, 1);
        gridPane.add(airbaseRegionOutRoute, 0, row2);
        gridPane.add(airbaseRegionOurRouteValue, 1, row2);
        gridPane.setId("region-step-summary-grid");

        VBox vBox = new VBox(airbaseRegionTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }
}
