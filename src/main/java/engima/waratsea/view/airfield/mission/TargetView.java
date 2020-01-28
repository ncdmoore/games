package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.Mission;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetFriendlyAirfield;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.util.Optional;

public class TargetView {

    private Label distance = new Label("Distance:");
    private Label distanceValue = new Label();
    private Label inRoute = new Label("Steps in route:");
    private Label inRouteValue = new Label();
    private Label capacity = new Label("Capacity in steps:");
    private Label capacityValue = new Label();
    private Label current = new Label("Stationed steps:");
    private Label currentValue = new Label();

    @Setter
    private Airbase airbase;

    @Setter
    private TableView<Mission> missions;

    private ViewProps props;

    private int inRouteSteps;

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
        airbase = base;

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

        Label title = new Label("Target Details:");
        VBox vBox = new VBox(title, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }

    /**
     * Show the target view.
     *
     * @param target The airbase's mission's target.
     */
    public void show(final Target target) {

        Optional.ofNullable(target).ifPresent(t -> {
            inRouteSteps = t.getTotalSteps(airbase) + getMissionTableSteps(t);

            distanceValue.setText(t.getDistance(airbase) + "");
            inRouteValue.setText(inRouteSteps + "");
            capacityValue.setText(t.getTotalCapacitySteps() + "");
            currentValue.setText(t.getCurrentSteps() + "");


            if (target.getClass() == TargetFriendlyAirfield.class) {
                capacity.setVisible(true);
                capacityValue.setVisible(true);
                current.setVisible(true);
                currentValue.setVisible(true);
            } else {
                capacity.setVisible(false);
                capacityValue.setVisible(false);
                current.setVisible(false);
                currentValue.setVisible(false);
            }
        });
    }

    /**
     * Add a squadron to the mission with this as a target.
     *
     * @param squadron The squadron assigned to the mission for this target.
     */
    public void addSquadron(final Squadron squadron) {
        inRouteSteps += squadron.getSteps().intValue();
        inRouteValue.setText(inRouteSteps + "");
    }

    /**
     * Remove a squadron from the mission with this as a target.
     *
     * @param squadron The squadron removed from the mission for this target.
     */
    public void removeSquadron(final Squadron squadron) {
        inRouteSteps -= squadron.getSteps().intValue();
        inRouteValue.setText(inRouteSteps + "");
    }

    /**
     * Get the steps in route to the target from this airbase's mission table.
     *
     * @param target The airbase's mission's target.
     * @return The number of steps in route to this target from this airbase.
     */
    private int getMissionTableSteps(final Target target) {
        return missions.getItems()
                .stream()
                .filter(mission -> mission.getTarget().isEqual(target))
                .map(Mission::getSteps)
                .reduce(0, Integer::sum);
    }
}
