package engima.waratsea.view.util;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

import java.util.Map;

public class TitledGridPane extends TitledPane {
    private int width;
    private String styleId;

    //private Map<String, Label> labelMap = new HashMap<>();

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
    public TitledGridPane setStyleId(final String value) {
        styleId = value;
        return this;
    }

    /**
     * Build a titled pane that contains a grid pane of the given data.
     *
     * @param title The titled pane's title.
     * @param data The data contained within the titled pane's grid pane.
     * @return A titled pane with the given data displayed in a grid pane.
     */
    public TitledGridPane buildPane(final String title, final Map<String, String> data) {
        setText(title);
        setContent(buildGrid(data));
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
     * Build a grid pane that contains the given data.
     *
     * @param data A map of key, value pairs that serves as the source of the grid data.
     * @return A grid pane. Each row contains two
     * columns. The first column is the key and the second the value.
     */
    private Node buildGrid(final Map<String, String> data) {
        GridPane gridPane = new GridPane();
        int i = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            Label keyLabel = new Label(entry.getKey());
            gridPane.add(keyLabel, 0, i);
            gridPane.add(new Label(entry.getValue()), 1, i);
            i++;

            //labelMap.put(entry.getKey(), keyLabel);
        }

        gridPane.getStyleClass().add(styleId);
        return gridPane;
    }
}
