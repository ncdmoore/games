package engima.waratsea.view.airfield.patrol;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.patrol.PatrolType;
import engima.waratsea.model.base.airfield.patrol.stats.PatrolStat;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.taskForce.patrol.PatrolGroup;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.weather.SmallWeatherView;
import engima.waratsea.viewmodel.airfield.AirbaseViewModel;
import engima.waratsea.viewmodel.airfield.PatrolViewModel;
import engima.waratsea.viewmodel.squadrons.SquadronViewModel;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Patrol stats view.
 *
 * CSS styles used.
 *
 * - patrol-stats-grid
 * - title-pane-{nation}
 * - patrol-note
 * - patrol-details-header
 * - patrol-details-row-header
 * - patrol-stats-cell
 */
public class PatrolStatsView {
    private final ViewProps props;

    private final SmallWeatherView weatherView;

    private final VBox vBox = new VBox();
    private final HBox hBox = new HBox();
    private final GridPane gridPane = new GridPane();

    private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param weatherView The weather view.
     */
    @Inject
    public PatrolStatsView(final ViewProps props,
                           final SmallWeatherView weatherView) {
        this.props = props;
        this.weatherView = weatherView;

        vBox.setId("patrol-stats-pane");
        hBox.setId("patrol-stats-hbox");
        gridPane.setId("patrol-stats-grid");
    }

    /**
     * Build the patrol stats view.
     *
     * @param selectedNation The nation: BRITISH, ITALIAN, etc...
     * @return A node containing the patrol stats.
     */
    public Node build(final Nation selectedNation) {
        nation = selectedNation;
        return vBox;
    }

    /**
     * Bind to the view model.
     *
     * @param viewModel The patrol view model.
     * @param patrolType The patrol type.
     */
    public void bind(final AirbaseViewModel viewModel, final PatrolType patrolType) {
        PatrolViewModel patrolViewModel = viewModel
                .getPatrolViewModels()
                .get(patrolType);

        patrolViewModel
                .getAssignedAllNations()
                .addListener((ListChangeListener<SquadronViewModel>) c -> setAssigned(viewModel, patrolType));

        setAssigned(viewModel, patrolType);

        weatherView.bind(patrolViewModel.getIsAffectedByWeather());
    }

    /**
     * Update the patrol stats.
     *
     * @param viewModel The airbase view model.
     * @param patrolType The patrol type.
     */
    private void setAssigned(final AirbaseViewModel viewModel, final PatrolType patrolType) {
        PatrolGroup patrolGroup = viewModel.getPatrolGroup(patrolType);

        Map<Integer, Map<String, PatrolStat>> stats = patrolGroup.getPatrolStats().getData();

        vBox.getChildren().clear();
        hBox.getChildren().clear();

        if (patrolGroup.areSquadronsPresent(nation)) {    // Squadrons are assigned to this patrol.
            String title = patrolGroup.getAirbaseGroup().getTitle();


            StackPane titlePane = new StackPane(new Label(title + " Patrol Average Statistics"));
            titlePane.setId("title-pane-" + nation.getFileName().toLowerCase());

            if (!stats.isEmpty()) {  // This patrol is effective.
                int max = patrolGroup.getTrueMaxRadius();
                int min = stats.keySet().stream().min(Integer::compareTo).orElse(0);
                int med = max / 2;

                Set<String> headers = stats.get(min).keySet();

                Map<String, Integer> ranges = getRanges(min, med, max);

                buildHeaders(headers);
                buildRows(stats, ranges);

                hBox.getChildren().add(gridPane);
            } else {
                Label label = new Label("Patrol is ineffective due to weather and/or its strength");
                label.getStyleClass().add("patrol-note");
                hBox.getChildren().add(label);
            }

            Node imageBox = weatherView.build();
            hBox.getChildren().add(imageBox);
            vBox.getChildren().addAll(titlePane, hBox);
        }
    }

    /**
     * Build the statistic's headers.
     *
     * @param headers The header strings.
     */
    private void buildHeaders(final Set<String> headers) {
        Label blankCorner = getHeaderLabel("");
        Label radiusHeader = getHeaderLabel("Radius");

        gridPane.add(blankCorner, 0, 0);
        gridPane.add(radiusHeader, 1, 0);

        int row = 0;
        int col = 2;
        for (String header : headers) {
            Label headerLabel = getHeaderLabel(header);
            gridPane.add(headerLabel, col, row);
            col++;
        }
    }

    /**
     * Build the statistic's rows.
     *
     * @param stats The statistic data.
     * @param ranges The minimum, median and maximum ranges.
     */
    private void buildRows(final Map<Integer, Map<String, PatrolStat>> stats, final Map<String, Integer> ranges) {
        int row = 1;
        for (Map.Entry<String, Integer> entry : ranges.entrySet()) {
            Label rowLabel = getRowHeaderLabel(entry.getKey());
            Label radiusLabel = getCellLabel(entry.getValue() + "");

            gridPane.add(rowLabel, 0, row);
            gridPane.add(radiusLabel, 1, row);

            buildPatrolStatRow(stats.get(entry.getValue()), row);
            row++;
        }
    }

    /**
     * Build a row in the patrol stats grid.
     *
     * @param row The row of stats.
     * @param rowIndex The row index.
     */
    private void buildPatrolStatRow(final Map<String, PatrolStat> row, final int rowIndex) {
        int col = 2;
        for (Map.Entry<String, PatrolStat> rowEntry : row.entrySet()) {
            Label label = getCellLabel(rowEntry.getValue().getValue());

            if (rowEntry.getValue().getFactors() != null) {
                label.setTooltip(new Tooltip(rowEntry.getValue().getFactors()));
            }

            gridPane.add(label, col, rowIndex);
            col++;
        }
    }

    private Map<String, Integer> getRanges(final int min, final int med, final int max) {
        Map<String, Integer> ranges = new LinkedHashMap<>();

        ranges.put("Minimum", min);
        if (med > 0 && med != min && med != max) {
            ranges.put("Median", med);
        }
        if (max > 0) {
            ranges.put("Maximum", max);
        }

        return ranges;
    }

    private Label getHeaderLabel(final String text) {
        Label label = new Label(text);
        label.setMaxWidth(props.getInt("patrol.grid.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.label.width"));
        label.getStyleClass().add("patrol-details-header");
        return label;
    }

    private Label getRowHeaderLabel(final String text) {
        Label label = new Label(text);
        label.setMaxWidth(props.getInt("patrol.grid.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.label.width"));
        label.getStyleClass().add("patrol-details-row-header");
        return label;
    }

    private Label getCellLabel(final String text) {
        Label label = new Label(text);
        label.setMaxWidth(props.getInt("patrol.grid.label.width"));
        label.setMinWidth(props.getInt("patrol.grid.label.width"));
        label.getStyleClass().add("patrol-stats-cell");
        return label;
    }
}
