package engima.waratsea.view.airfield.mission.stats;

import com.google.inject.Inject;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronHome;
import engima.waratsea.model.target.Target;
import engima.waratsea.presenter.airfield.mission.MissionStats;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


public class FerryView implements StatsView {
    private Label airbaseTitle = new Label();
    private Label distanceValue = new Label();
    private Label inRouteValue = new Label();
    private Label capacityValue = new Label();
    private Label currentValue = new Label();
    private Label targetRegionTitle = new Label();
    private Label targetRegionMaxStepsValue = new Label();
    private Label targetRegionCurrentStepsValue = new Label();
    private Label targetRegionInRouteValue = new Label();
    private Label airbaseRegionTitle = new Label();
    private Label airbaseRegionMinStepsValue = new Label();
    private Label airbaseRegionCurrentStepsValue = new Label();
    private Label airbaseRegionOurRouteValue = new Label();

    private ViewProps props;
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
    public Node build() {
        Node airbaseStats = buildAirbaseStats();
        Node targetRegionStats = buildTargetRegionStats();
        Node airbaseRegionStats = buildAirbaseRegionStats();

        VBox statsVBox = new VBox(airbaseStats, targetRegionStats, airbaseRegionStats);
        statsVBox.setId("target-stats-vbox");
        return statsVBox;
    }

    /**
     * Show the mission stats view.
     *
     * @param missionStats The mission stats.
     */
    @Override
    public void show(final MissionStats missionStats) {
        airbaseTitle.setText(missionStats.getTargetStats().getTitle());
        distanceValue.setText(missionStats.getTargetStats().getDistance());
        inRouteValue.setText(missionStats.getTargetStats().getRouteSteps());
        capacityValue.setText(missionStats.getTargetStats().getCapacitySteps());
        currentValue.setText(missionStats.getTargetStats().getCurrentSteps());

        targetRegionTitle.setText(missionStats.getTargetRegionStats().getTitle());
        targetRegionMaxStepsValue.setText(missionStats.getTargetRegionStats().getMaxSteps());
        targetRegionCurrentStepsValue.setText(missionStats.getTargetRegionStats().getCurrentSteps());
        targetRegionInRouteValue.setText(missionStats.getTargetRegionStats().getRouteSteps());

        airbaseRegionTitle.setText(missionStats.getAirfieldRegionStats().getTitle());
        airbaseRegionMinStepsValue.setText(missionStats.getAirfieldRegionStats().getMinSteps());
        airbaseRegionCurrentStepsValue.setText(missionStats.getAirfieldRegionStats().getCurrentSteps());
        airbaseRegionOurRouteValue.setText(missionStats.getAirfieldRegionStats().getRouteSteps());
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
        int regionInRoute = Integer.parseInt(targetRegionInRouteValue.getText());
        int regionOutRoute = Integer.parseInt(airbaseRegionOurRouteValue.getText());

        inRouteValue.setText(inRoute + squadron.getSteps().intValue() + "");

        Nation nation = squadron.getNation();
        SquadronHome airbase = squadron.getHome();

        if (airbase.getRegion(nation) != target.getRegion(nation)) {
            targetRegionInRouteValue.setText(regionInRoute + squadron.getSteps().intValue() + "");
            airbaseRegionOurRouteValue.setText(regionOutRoute + squadron.getSteps().intValue() + "");
        }
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
        int regionInRoute = Integer.parseInt(targetRegionInRouteValue.getText());
        int regionOutRoute = Integer.parseInt(airbaseRegionOurRouteValue.getText());

        inRouteValue.setText(inRoute - squadron.getSteps().intValue() + "");

        Nation nation = squadron.getNation();
        SquadronHome airbase = squadron.getHome();

        if (airbase.getRegion(nation) != target.getRegion(nation)) {
            targetRegionInRouteValue.setText(regionInRoute - squadron.getSteps().intValue() + "");
            airbaseRegionOurRouteValue.setText(regionOutRoute - squadron.getSteps().intValue() + "");
        }
    }

    /**
     * Build the airbase stats.
     *
     * @return A node containing the airbase stats.
     */
    private Node buildAirbaseStats() {
        Label distance = new Label("Distance:");
        Label inRoute = new Label("Steps in route:");
        Label capacity = new Label("Capacity in steps:");
        Label current = new Label("Stationed steps:");

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
        gridPane.add(airbaseRegionOurRouteValue, 1, row2);
        gridPane.setId("region-step-summary-grid");

        VBox vBox = new VBox(airbaseRegionTitle, gridPane);
        vBox.setMaxWidth(props.getInt("airfield.dialog.mission.list.width"));
        vBox.setMinWidth(props.getInt("airfield.dialog.mission.list.width"));

        return vBox;
    }
}
