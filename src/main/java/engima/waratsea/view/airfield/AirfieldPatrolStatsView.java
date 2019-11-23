package engima.waratsea.view.airfield;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.airfield.patrol.Patrol;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.weather.Weather;
import engima.waratsea.utility.ImageResourceProvider;
import engima.waratsea.view.ViewProps;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AirfieldPatrolStatsView {

    private final ViewProps props;
    private final ImageResourceProvider imageProvider;

    @Setter
    private Airfield airfield;

    @Setter
    private PatrolType patrolType;

    private final Weather weather;

    private VBox vBox = new VBox();
    private HBox hBox = new HBox();
    private GridPane gridPane = new GridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     * @param imageProvider Provides images.
     * @param weather The current weather.
     */
    @Inject
    public AirfieldPatrolStatsView(final ViewProps props,
                                   final ImageResourceProvider imageProvider,
                                   final Weather weather) {
        this.props = props;
        this.imageProvider = imageProvider;
        this.weather = weather;
        vBox.setId("patrol-stats-pane");
        hBox.setId("patrol-stats-hbox");
        gridPane.setId("patrol-stats-grid");
    }

    /**
     * Build the patrol stats view.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param assigned The current squadrons assigned to the patrol.
     * @return A node containing the patrol stats.
     */
    public Node buildPatrolStats(final Nation nation, final List<Squadron> assigned) {

        Patrol patrol = airfield.getTemporaryPatrol(patrolType, assigned);

        Map<Integer, Map<String, String>> stats = patrol.getPatrolStats();

        vBox.getChildren().clear();
        hBox.getChildren().clear();

        if (!stats.isEmpty()) {
            StackPane titlePane = new StackPane(new Label("Patrol Average Statistics"));
            titlePane.setId("summary-title-pane-" + nation.getFileName().toLowerCase());

            int max = patrol.getTrueMaxRadius();
            int min = stats.keySet().stream().min(Integer::compareTo).orElse(0);
            int med = max / 2;

            Set<String> headers = stats.get(min).keySet();

            buildHeaders(headers);

            Map<String, Integer> ranges = new LinkedHashMap<>();

            ranges.put("Minimum", min);
            if (med > 0 && med != min && med != max) {
                ranges.put("Median", med);
            }
            if (max > 0) {
                ranges.put("Maximum", max);
            }

            buildRows(stats, ranges);

            Node imageBox = buildWeather(patrol);

            hBox.getChildren().add(gridPane);
            hBox.getChildren().add(imageBox);

            vBox.getChildren().addAll(titlePane, hBox);
        }

        return vBox;
    }

    /**
     * Update the patrol stats.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @param assigned The current squqdrons assigned to the patrol.
     */
    public void updatePatrolStats(final Nation nation, final List<Squadron> assigned) {
        gridPane.getChildren().clear();
        buildPatrolStats(nation, assigned);
    }

    /**
     * Build the weather image.
     *
     * @param patrol The patrol.
     *  @return The node containing the weather image.
     */
    private Node buildWeather(final Patrol patrol) {

        boolean affectedByWeather = patrol.isAffectedByWeather();
        String text = affectedByWeather ? "Affected by Weather" : "No Weather Affect";
        Label label = new Label(text);
        Paint paint = affectedByWeather ? Color.RED : Color.BLACK;
        label.setTextFill(paint);

        ImageView image = imageProvider.getImageView(weather.getCurrent().toString().toLowerCase() + "50x50.png");
        VBox imageBox = new VBox(label, image);

        imageBox.setId("patrol-weather-box");

        return imageBox;
    }

    /**
     * Build the statistic's headers.
     *
     * @param headers The header strings.
     */
    private void buildHeaders(final Set<String> headers) {
        Label radiusHeader = new Label("Radius");
        radiusHeader.setMaxWidth(props.getInt("patrol.grid.label.width"));
        radiusHeader.setMinWidth(props.getInt("patrol.grid.label.width"));
        radiusHeader.setId("patrol-details-header");
        gridPane.add(radiusHeader, 1, 0);

        Label blankCorner = new Label("");
        blankCorner.setMaxWidth(props.getInt("patrol.grid.label.width"));
        blankCorner.setMinWidth(props.getInt("patrol.grid.label.width"));
        blankCorner.setId("patrol-details-header");
        gridPane.add(blankCorner, 0, 0);

        int row = 0;
        int col = 2;
        for (String header : headers) {
            Label headerLabel = new Label(header);
            headerLabel.setMaxWidth(props.getInt("patrol.grid.label.width"));
            headerLabel.setMinWidth(props.getInt("patrol.grid.label.width"));
            headerLabel.setId("patrol-details-header");
            gridPane.add(headerLabel, col, row);
            col++;
        }
    }

    /**
     * Build the statistic's rows.
     *
     * @param stats The statistic data.
     * @param ranges The mininum, median and maximum ranges.
     */
    private void buildRows(final Map<Integer, Map<String, String>> stats, final Map<String, Integer> ranges) {
        int row = 1;
        for (Map.Entry<String, Integer> entry : ranges.entrySet()) {
            Label rowLabel = new Label(entry.getKey());
            rowLabel.setMaxWidth(props.getInt("patrol.grid.label.width"));
            rowLabel.setMinWidth(props.getInt("patrol.grid.label.width"));
            rowLabel.setId("patrol-details-row-header");
            gridPane.add(rowLabel, 0, row);

            Label radiusLabel = new Label(entry.getValue() + "");
            radiusLabel.setMaxWidth(props.getInt("patrol.grid.label.width"));
            radiusLabel.setMinWidth(props.getInt("patrol.grid.label.width"));
            radiusLabel.setId("patrol-stats-cell");

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
    private void buildPatrolStatRow(final Map<String, String> row, final int rowIndex) {
        int col = 2;
        for (Map.Entry<String, String> rowEntry : row.entrySet()) {
            Label label = new Label(rowEntry.getValue());
            label.setMaxWidth(props.getInt("patrol.grid.label.width"));
            label.setMinWidth(props.getInt("patrol.grid.label.width"));
            label.setId("patrol-stats-cell");
            gridPane.add(label, col, rowIndex);
            col++;
        }
    }
}
