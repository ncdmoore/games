package engima.waratsea.view.util;

import javafx.beans.property.IntegerProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class BoundGridPaneMap {
    private int width;

    @Setter private String gridStyleId;
    @Setter private List<ColumnConstraints> columnConstraints;


    /**
     * Set the titled pane's width.
     *
     * @param value the value of the titled pane's width.
     * @return This titled pane.
     */
    public BoundGridPaneMap setWidth(final int value) {
        width = value;
        return this;
    }


    /**
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    public Node buildAndBind(final Map<String, IntegerProperty> data) {
        GridPane gridPane = new GridPane();

        int i = 0;
        for (Map.Entry<String, IntegerProperty> entry : data.entrySet()) {
            Label keyLabel = new Label(entry.getKey());
            Label valueLabel = new Label();

            valueLabel.textProperty().bind(entry.getValue().asString());

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
}
