package engima.waratsea.view.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridPaneMap {
    private int width;
    private GridPane gridPane;
    private final Map<String, Label> gridValues = new HashMap<>();

    @Setter private String gridStyleId;
    @Setter private List<ColumnConstraints> columnConstraints;

    /**
     * Set the titled pane's width.
     *
     * @param value the value of the titled pane's width.
     * @return This titled pane.
     */
    public GridPaneMap setWidth(final int value) {
        width = value;
        return this;
    }

    /**
     * Build the grid.
     *
     * @return The grid pane.
     */
    public Node buildGrid() {
        gridPane = new GridPane();
        return gridPane;
    }

    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    public Node buildGrid(final Map<String, String> data) {
        gridPane = new GridPane();
        int i = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Label keyLabel = new Label(entry.getKey());
            Label valueLabel = new Label(entry.getValue());

            gridValues.put(entry.getKey(), valueLabel);

            gridPane.add(keyLabel, 0, i);
            gridPane.add(valueLabel, 1, i);
            i++;
        }

        if (StringUtils.isNotBlank(gridStyleId)) {
            gridPane.getStyleClass().add(gridStyleId);
        }

        if (columnConstraints != null) {
            gridPane.getColumnConstraints().addAll(columnConstraints);
        }

        gridPane.setMaxWidth(width);
        gridPane.setMinWidth(width);

        return gridPane;
    }

    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    public Node buildGridMultiColumn(final Map<String, List<String>> data) {
        gridPane = new GridPane();
        int row = 0;
        for (Map.Entry<String, List<String>> entry : data.entrySet()) {
            Label keyLabel = new Label(entry.getKey());
            gridPane.add(keyLabel, 0, row);
            for (int column = 0; column < entry.getValue().size(); column++) {
                gridPane.add(new Label(entry.getValue().get(column)), column + 1, row);
            }
            row++;
        }

        if (StringUtils.isNotBlank(gridStyleId)) {
            gridPane.getStyleClass().add(gridStyleId);
        }

        if (columnConstraints != null) {
            gridPane.getColumnConstraints().addAll(columnConstraints);
        }

        gridPane.setMaxWidth(width);
        gridPane.setMinWidth(width);

        return gridPane;
    }

    /**
     * Update the grid.
     *
     * @param data The grid's new data.
     */
    public void updateGrid(final Map<String, String> data) {
        int i = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Label keyLabel = new Label(entry.getKey());
            Label valueLabel = new Label(entry.getValue());

            gridValues.put(entry.getKey(), valueLabel);

            gridPane.add(keyLabel, 0, i);
            gridPane.add(valueLabel, 1, i);
            i++;
        }

        if (StringUtils.isNotBlank(gridStyleId)) {
            gridPane.getStyleClass().add(gridStyleId);
        }

        if (columnConstraints != null) {
            gridPane.getColumnConstraints().addAll(columnConstraints);
        }

        gridPane.setMaxWidth(width);
        gridPane.setMinWidth(width);
    }

    /**
     * Update an individual grid value.
     *
     * @param key The grid key. The first columns's value.
     * @param value The grid value. The second columns's value.
     */
    public void updateGrid(final String key, final String value) {
        gridValues.get(key).setText(value);
    }
}
