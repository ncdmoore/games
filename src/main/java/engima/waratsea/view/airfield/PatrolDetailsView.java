package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the patrol details view. Displayed when a patrol radius circle is clicked on the game map.
 */
public class PatrolDetailsView {
    private final ViewProps props;

    @Getter
    private final Map<PatrolType, List<Label>> labelMap = new HashMap<>();

    @Getter
    private final TabPane patrolsTabPane = new TabPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public PatrolDetailsView(final ViewProps props) {
        this.props = props;

        Stream.of(PatrolType.values())
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

        GridPane gridPane = new GridPane();
        gridPane.setId("patrol-details-grid");

        labelMap.get(PatrolType.getType(patrol)).clear();

        Map<Integer, Map<String, String>> data = patrol.getPatrolStats();

        buildHeaders(gridPane, data.get(1));

        int row = 1;
        for (Map.Entry<Integer, Map<String, String>> entry : data.entrySet()) {
            Label radiusLabel = buildRow(gridPane, entry, row);
            labelMap.get(PatrolType.getType(patrol)).add(radiusLabel);
            row++;
        }

        VBox vBox = new VBox(gridPane);

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setId("patrol-details-pane");

        tab.setContent(scrollPane);

        return tab;
    }

    /**
     * Build the patrol details grid header.
     *
     * @param gridPane The grid pane that houses the grid.
     * @param data The data to place in the grid, used to get the label text of the headers.
     */
    private void buildHeaders(final GridPane gridPane, final Map<String, String> data) {
        Label label = new Label("Radius");
        label.setMaxWidth(props.getInt("patrol.grid.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.label.width"));
        label.setId("patrol-details-header");
        gridPane.add(label, 0, 0);

        int col = 1;
        for (String title: data.keySet()) {
            label = new Label(title);
            label.setMaxWidth(props.getInt("patrol.grid.label.width"));
            label.setMinWidth(props.getInt("patrol.grid.label.width"));
            label.setId("patrol-details-header");
            gridPane.add(label, col, 0);
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
    private Label buildRow(final GridPane gridPane, final Map.Entry<Integer, Map<String, String>> entry, final int row) {
        int radius = entry.getKey();
        Map<String, String> data = entry.getValue();

        Label radiusLabel = new Label(radius + "");
        radiusLabel.setMaxWidth(props.getInt("patrol.grid.label.width"));
        radiusLabel.setMinWidth(props.getInt("patrol.grid.label.width"));
        radiusLabel.setId("patrol-details-cell");
        radiusLabel.setUserData(radius);

        gridPane.add(radiusLabel, 0, row);
        int col = 1;
        for (Map.Entry<String, String> dataEntry : data.entrySet()) {
            Label label = new Label(dataEntry.getValue());
            label.setMaxWidth(props.getInt("patrol.grid.label.width"));
            label.setMinWidth(props.getInt("patrol.grid.label.width"));
            label.setId("patrol-details-cell");
            gridPane.add(label, col, row);
            col++;
        }

        return radiusLabel;
    }
}
