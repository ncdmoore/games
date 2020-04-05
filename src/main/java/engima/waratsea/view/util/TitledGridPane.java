package engima.waratsea.view.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TitledGridPane extends TitledPane {
    private int width;
    private String gridStyleId;

    @Getter private GridPane gridPane;
    private Map<String, Label> gridValues = new HashMap<>();
    /**
     * Set the titled pane's width.
     *
     * @param value the value of the titled pane's width.
     * @return This titled pane.
     */
    public TitledGridPane setWidth(final int value) {
        width = value;
        return this;
    }

    /**
     * Set whether the pane may be collapsed.
     *
     * @param value The collapsible value.
     * @return This titled pane.
     */
    public TitledGridPane setCollapse(final boolean value) {
        super.setCollapsible(value);
        return this;
    }

    /**
     * Set the titled pane's CSS style Id.
     *
     * @param value the value of the titled pane's CSS style Id.
     * @return This titled pane.
     */
    public TitledGridPane setGridStyleId(final String value) {
        gridStyleId = value;
        return this;
    }

    /**
     * Set the titled pane's title.
     *
     * @param value The value of the title.
     * @return This titled pane.
     */
    public TitledGridPane setTitle(final String value) {
        setText(value);
        return this;
    }

    /**
     * Build an empty titled pane.
     *
     * @return An empty titled pane.
     */
    public TitledGridPane buildPane() {
        gridPane = new GridPane();
        setMinWidth(width);
        setMaxWidth(width);
        setCollapsible(true);
        return this;
    }

    /**
     * Build a titled pane that contains a grid pane of the given data.
     *
     * @param data The data contained within the titled pane's grid pane.
     * @return A titled pane with the given data displayed in a grid pane.
     */
    public TitledGridPane buildPane(final Map<String, String> data) {
        gridPane = new GridPane();
        setContent(buildGrid(data));
        setMinWidth(width);
        setMaxWidth(width);
        setCollapsible(true);
        return this;
    }

    /**
     * Build a titled pane that contains a grid pane of the given data.
     *
     * @param data The data contained within the titled pane's grid pane.
     * @return A titled pane with the given data displayed in a grid pane.
     */
    public TitledGridPane buildPaneMultiColumn(final Map<String, List<String>> data) {
        gridPane = new GridPane();
        setContent(buildGridMultiColumn(data));
        setMinWidth(width);
        setMaxWidth(width);
        setCollapsible(true);
        return this;
    }

    /**
     * Update the titled pane grid pane contents with the given data.
     *
     * @param data The data contained within the titled pane's grid pane.
     */
    public void updatePane(final Map<String, String> data) {
        setContent(buildGrid(data));
    }

    /**
     * Update an individual grid value.
     *
     * @param key The grid key. The first columns's value.
     * @param value The grid value. The secod columns's value.
     */
    public void updateGrid(final String key, final String value) {
        gridValues.get(key).setText(value);
    }

    /**
     * Update the titled pane grid pane contents with the given data.
     *
     * @param data The data contained within the titled pane's grid pane.
     */
    public void updatePaneMultiColumn(final Map<String, List<String>> data) {
        setContent(buildGridMultiColumn(data));
    }


    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    private Node buildGrid(final Map<String, String> data) {
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

        return gridPane;
    }

    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    private Node buildGridMultiColumn(final Map<String, List<String>> data) {
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

        return gridPane;
    }
}
