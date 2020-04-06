package engima.waratsea.view.util;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class TitledGridPane extends TitledPane {
    private int width;
    private String gridStyleId;
    @Setter private List<ColumnConstraints> columnConstraints;
    @Getter private GridPaneMap gridPane;

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
        if (width > 0) {
            setMinWidth(width);
            setMaxWidth(width);
        }

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
        setContent(buildGrid(data));

        if (width > 0) {
            setMinWidth(width);
            setMaxWidth(width);
        }

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
        setContent(buildGridMultiColumn(data));

        if (width > 0) {
            setMinWidth(width);
            setMaxWidth(width);
        }

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
     * @param value The grid value. The second columns's value.
     */
    public void updateGrid(final String key, final String value) {
        gridPane.updateGrid(key, value);
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
        gridPane = new GridPaneMap();
        gridPane.setGridStyleId(gridStyleId);
        gridPane.setColumnConstraints(columnConstraints);
        gridPane.setWidth(width);
        return gridPane.buildGrid(data);
    }

    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    private Node buildGridMultiColumn(final Map<String, List<String>> data) {
        gridPane = new GridPaneMap();
        gridPane.setGridStyleId(gridStyleId);
        gridPane.setColumnConstraints(columnConstraints);
        gridPane.setWidth(width);
        return gridPane.buildGridMultiColumn(data);
    }
}
