package engima.waratsea.view.airfield.mission;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftType;
import engima.waratsea.model.base.airfield.mission.AirMission;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.airfield.mission.stats.ProbabilityStatsView;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents the mission details view. Displayed when a mission arrow is clicked on the game map.
 */
public class MissionDetailsView {
    private final ViewProps props;

    @Getter private final Map<PatrolType, List<Label>> labelMap = new HashMap<>();
    @Getter private final TabPane missionsTabPane = new TabPane();

    private final ProbabilityStatsView statsView;

    /**
     * Constructor called by guice.
     *
     * @param statsView The mission statistics view.
     * @param props The view properties.
     */
    @Inject
    public MissionDetailsView(final ProbabilityStatsView statsView,
                              final ViewProps props) {
        this.statsView = statsView;
        this.props = props;

        PatrolType.stream()
                .forEach(patrolType -> labelMap.put(patrolType, new ArrayList<>()));
    }

    /**
     * Show the airfield details.
     *
     * @param missions The airfield's missions that are represented by the arrow that was clicked.
     * @return A node containing the mission details.
     */
    public Node show(final List<AirMission> missions) {
        missionsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = missions
                .stream()
                .map(this::buildTab)
                .collect(Collectors.toList());

        missionsTabPane.getTabs().addAll(tabs);

        return missionsTabPane;
    }

    /**
     * Build a tab for a type of air mission.
     *
     * @param mission A given air mission.
     * @return A tab for the given mission.
     */
    private Tab buildTab(final AirMission mission) {
        Tab tab = new Tab("No: " + mission.getId());

        Node summaryNode = buildSummary(mission);
        Node squadronSummaryNode = buildSquadronSummary(mission);
        Node squadronDetailsNode = buildSquadronDetails(mission);

        Node statsNode = statsView.setHorizontal().build(mission.getMissionProbability());

        VBox vBox = new VBox(summaryNode, squadronSummaryNode, squadronDetailsNode, statsNode);
        vBox.setId("mission-details-vbox");
        ScrollPane scrollPane = new ScrollPane(vBox);

        tab.setContent(scrollPane);
        return tab;
    }

    /**
     * Build a node containing a summary of the mission.
     *
     * @param mission The mission.
     * @return A node containing summary information about the given mission.
     */
    private Node buildSummary(final AirMission mission) {
        Label missionLabel = new Label("Mission:");
        Label missionValue = new Label(mission.getType().toString());
        Label targetLabel = new Label("Target:");
        Label targetValue = new Label(mission.getTarget().getTitle());

        Label squadronCountLabel = new Label("Squadrons:");
        Label squadronCountValue = new Label(mission.getSquadrons().getAll().size() + "");
        GridPane gridPane = new GridPane();
        gridPane.add(missionLabel, 0, 0);
        gridPane.add(missionValue, 1, 0);
        gridPane.add(targetLabel, 0, 1);
        gridPane.add(targetValue, 1, 1);
        gridPane.add(squadronCountLabel, 0, 2);
        gridPane.add(squadronCountValue, 1, 2);

        gridPane.setId("mission-details-summary-grid");

        return gridPane;
    }

    /**
     * Build the mission squadron summary.
     *
     * @param mission The mission.
     * @return A node containing a summary of the mission's squadrons.
     */
    private Node buildSquadronSummary(final AirMission mission) {
        Map<AircraftType, List<Squadron>> squadronTypeMap = mission
                .getSquadrons()
                .getAll()
                .stream()
                .collect(Collectors.groupingBy(Squadron::getType));

        GridPane gridPane = new GridPane();

        AtomicInteger column = new AtomicInteger();

        AircraftType.stream().forEach(type -> {
            int col = column.getAndIncrement();

            Label header = new Label(type.toString());
            styleHeaderLabel(header);
            gridPane.add(header, col, 0);

            Optional<List<Squadron>> squadrons = Optional
                    .ofNullable(squadronTypeMap.get(type));

            int size = squadrons
                    .map(List::size)
                    .orElse(0);

            Label count = new Label(size + "");
            styleRowLabel(count);

            String names = squadrons
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(Squadron::getTitle)
                    .collect(Collectors.joining("\n"));

            if (StringUtils.isNotBlank(names)) {
                count.setTooltip(new Tooltip(names));
            }

            gridPane.add(count, col, 1);
        });

        gridPane.getStyleClass().add("mission-details-squadron-grid");

        Label title = new Label("Mission Squadron Summary:");
        return new VBox(title, gridPane);
    }

    /**
     * Build the squadron details.
     *
     * @param mission The mission.
     * @return The squadron details grid.
     **/
    private Node buildSquadronDetails(final AirMission mission) {
        GridPane gridPane = new GridPane();

        buildDetailsHeaderRow(gridPane);
        buildDetailsDataRow(gridPane, mission);

        gridPane.getStyleClass().add("mission-details-squadron-grid");

        Label title = new Label("Mission Squadron Details:");
        return new VBox(title, gridPane);
    }

    private void buildDetailsHeaderRow(final GridPane gridPane) {
        List<String> headers = List.of("Name", "Type", "Airbase", "Strength", "Radius");

        AtomicInteger column = new AtomicInteger();

        headers.forEach(header -> {
            int col = column.getAndIncrement();
            Label headerLabel = new Label(header);
            styleHeaderLabel(headerLabel);
            gridPane.add(headerLabel, col, 0);
        });
    }

    private void buildDetailsDataRow(final GridPane gridPane, final AirMission mission) {
        List<Squadron> squadrons = mission.getSquadrons().getAll();

        AtomicInteger row = new AtomicInteger(1);

        squadrons.forEach(squadron -> {
            int r = row.getAndIncrement();
            AtomicInteger column = new AtomicInteger();

            List<String> rowData = List.of(squadron.getTitle(),
                    squadron.getType().toString(),
                    squadron.getHome().getTitle(),
                    squadron.getStrength().toString(),
                    squadron.getRadius() + "");

            rowData.forEach(rowValue -> {
                int col = column.getAndIncrement();
                Label rowLabel = new Label(rowValue);
                styleRowLabel(rowLabel);
                gridPane.add(rowLabel, col, r);
            });
        });
    }

    /**
     * Style the table's data row labels.
     *
     * @param label A label that is styled.
     */
    private void styleHeaderLabel(final Label label) {
        label.setMaxWidth(props.getInt("mission.grid.details.label.width"));
        label.setMinWidth(props.getInt("mission.grid.details.label.width"));
        label.getStyleClass().add("mission-stats-header");
    }
    /**
     * Style the table's data row labels.
     *
     * @param label A label that is styled.
     */
    private void styleRowLabel(final Label label) {
        label.setMaxWidth(props.getInt("mission.grid.details.label.width"));
        label.setMinWidth(props.getInt("mission.grid.details.label.width"));
        label.getStyleClass().add("mission-details-cell");
    }
}
