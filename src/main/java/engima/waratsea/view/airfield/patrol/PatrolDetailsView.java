package engima.waratsea.view.airfield.patrol;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStat;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents the patrol details view. Displayed when a patrol radius circle is clicked on the game map.
 */
public class PatrolDetailsView {
    private final ViewProps props;

    @Getter private final Map<PatrolType, List<Label>> labelMap = new HashMap<>();
    @Getter private final TabPane patrolsTabPane = new TabPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public PatrolDetailsView(final ViewProps props) {
        this.props = props;

        PatrolType
                .stream()
                .forEach(patrolType -> labelMap.put(patrolType, new ArrayList<>()));
    }

    /**
     * Show the airfield details.
     *
     * @param patrols The airfield whose details are shown.
     * @return A node containing the airfield details.
     */
    public Node show(final List<Patrol> patrols) {
        patrolsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        List<Tab> tabs = patrols
                .stream()
                .map(this::buildTab)
                .collect(Collectors.toList());

        patrolsTabPane.getTabs().addAll(tabs);

        return patrolsTabPane;
    }

    /**
     * Build a tab for a type of patrol.
     *
     * @param patrol A given patrol.
     * @return A tab for the given patrol.
     */
    private Tab buildTab(final Patrol patrol) {
        Tab tab = new Tab(PatrolType.getTitle(patrol));

        Node squadronPane = buildSquadrons(patrol);
        Node radiiPane = buildRadii(patrol);

        VBox vBox = new VBox(squadronPane, radiiPane);
        vBox.setId("patrol-details-vbox");

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setId("patrol-details-pane");

        tab.setContent(scrollPane);

        return tab;
    }

    /**
     * Build the squadron summary.
     *
     * @param patrol The patrol.
     * @return The squadron summary grid.
     **/
    private Node buildSquadrons(final Patrol patrol) {
        GridPane gridPane = new GridPane();
        AtomicInteger column = new AtomicInteger();

        Map<AircraftBaseType, List<Squadron>> squadronTypeMap = patrol
                .getAssignedSquadrons()
                .stream()
                .collect(Collectors.groupingBy(Squadron::getBaseType));

        AircraftBaseType.stream().forEach(type -> {
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
            styleLabel(count);

            String names = squadrons
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(Squadron::getTitle)
                    .collect(Collectors.joining("\n"));

            count.setTooltip(new Tooltip(names));

            gridPane.add(count, col, 1);
        });

        gridPane.setId("patrol-details-grid");

        Label title = new Label("Patrol Squadron Summary:");
        return new VBox(title, gridPane);
    }

    /**
     * Build the patrol effectiveness radius table.
     *
     * @param patrol The patrol.
     * @return A Node containing the radius effectiveness grid.
     */
    private Node buildRadii(final Patrol patrol) {
        GridPane gridPane = new GridPane();
        gridPane.setId("patrol-details-grid");

        labelMap.get(PatrolType.getType(patrol)).clear();

        Map<Integer, Map<String, PatrolStat>> data = patrol.getPatrolStats().getData();

        buildHeaders(gridPane, patrol);

        int row = 1;
        for (Map.Entry<Integer, Map<String, PatrolStat>> entry : data.entrySet()) {
            Label radiusLabel = buildRow(gridPane, entry, row);
            labelMap.get(PatrolType.getType(patrol)).add(radiusLabel);
            row++;
        }

        Label title = new Label("Patrol Effectiveness:");

        return new VBox(title, gridPane);
    }

    /**
     * Build the patrol details grid header.
     *
     * @param gridPane The grid pane that houses the grid.
     * @param patrol The patrol.
     */
    private void buildHeaders(final GridPane gridPane, final Patrol patrol) {
        Map<String, PatrolStat> data = patrol.getPatrolStats().getData().get(1);

        Label label = new Label("Radius");
        styleHeaderLabel(label);
        gridPane.add(label, 0, 0);

        int col = 1;
        for (String title: data.keySet()) {
            label = new Label(title);
            styleHeaderLabel(label);
            gridPane.add(label, col, 0);

            String toolTip = patrol
                    .getPatrolStats()
                    .getMetaData()
                    .get(title);

            if (toolTip != null) {
                label.setTooltip(new Tooltip(toolTip));
            }

            col++;
        }
    }

    /**
     * Build the patrol details grid rows.
     *
     * @param gridPane The grid pane that houses the grid.
     * @param entry The data to place in the grid.
     * @param row The patrol radius.
     * @return The radius label.
     */
    private Label buildRow(final GridPane gridPane, final Map.Entry<Integer, Map<String, PatrolStat>> entry, final int row) {
        int radius = entry.getKey();
        Map<String, PatrolStat> data = entry.getValue();

        // Add the patrol radius label.
        Label radiusLabel = new Label(radius + "");
        styleLabel(radiusLabel);
        radiusLabel.setUserData(radius);
        gridPane.add(radiusLabel, 0, row);

        AtomicInteger col = new AtomicInteger(1);

        // Add the remaining row labels.
        data.forEach((key, stat) -> {
            Label label = new Label(stat.getValue());
            styleLabel(label);
            if (stat.getFactors() != null) {
                label.setTooltip(new Tooltip(stat.getFactors()));
            }
            gridPane.add(label, col.getAndIncrement(), row);
        });

        return radiusLabel;
    }

    /**
     * Style the table's data row labels.
     *
     * @param label A label that is styled.
     */
    private void styleHeaderLabel(final Label label) {
        label.setMaxWidth(props.getInt("patrol.grid.details.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.details.label.width"));
        label.setId("patrol-details-header");
    }
    /**
     * Style the table's data row labels.
     *
     * @param label A label that is styled.
     */
    private void styleLabel(final Label label) {
        label.setMaxWidth(props.getInt("patrol.grid.details.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.details.label.width"));
        label.setId("patrol-details-cell");
    }
}
