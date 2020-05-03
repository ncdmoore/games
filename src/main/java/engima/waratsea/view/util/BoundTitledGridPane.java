package engima.waratsea.view.util;

import javafx.beans.property.IntegerProperty;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.ColumnConstraints;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class BoundTitledGridPane extends TitledPane {
    private int width;
    private String gridStyleId;

    @Setter private List<ColumnConstraints> columnConstraints;
    @Getter private BoundGridPaneMap gridPane = new BoundGridPaneMap();

    /**
     * Set the titled pane's width.
     *
     * @param value the value of the titled pane's width.
     * @return This titled pane.
     */
    public BoundTitledGridPane setWidth(final int value) {
        width = value;
        return this;
    }

    /**
     * Set the titled pane's CSS style Id.
     *
     * @param value the value of the titled pane's CSS style Id.
     * @return This titled pane.
     */
    public BoundTitledGridPane setGridStyleId(final String value) {
        gridStyleId = value;
        return this;
    }

    /**
     * Set the titled pane's title.
     *
     * @param value The value of the title.
     * @return This titled pane.
     */
    public BoundTitledGridPane setTitle(final String value) {
        setText(value);
        return this;
    }

    /**
     * Build an empty titled pane.
     *
     * @return An empty titled pane.
     */
    public BoundTitledGridPane build() {
        if (width > 0) {
            setMinWidth(width);
            setMaxWidth(width);
        }

        gridPane.setGridStyleId(gridStyleId);
        gridPane.setColumnConstraints(columnConstraints);
        gridPane.setWidth(width);
        return this;
    }

    /**
     * Bind the grid data.
     *
     * @param data The data that is contained within the grid.
     * @return This titled pane.
     */
    public BoundTitledGridPane bind(final Map<String, IntegerProperty> data) {
        setContent(gridPane.buildAndBind(data));
        return this;
    }

}
