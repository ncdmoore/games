package engima.waratsea.view;

import engima.waratsea.view.util.BoundTitledGridPane;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

/**
 * Contains a an info view pane.
 *
 * CSS styles used.
 *
 * - component-grid
 */
public class InfoPane {

    private final BoundTitledGridPane infoPane = new BoundTitledGridPane();

    /**
     * Set the width.
     *
     * @param width
     * @return
     */
    public InfoPane setWidth(final int width) {
        infoPane.setWidth(width);
        return this;
    }

    /**
     * Set the row threshold property.
     *
     * @param threshold The row threshold property. If this is set to true then the grid will automatically be laid out
     *                  in two columns if the number of rows is greater than the row threshold.
     * @return This object.
     */
    public InfoPane setThreshold(final boolean threshold) {
        infoPane.setThreshold(threshold);
        return this;
    }

    /**
     * Build the squadron counts node.
     *
     * @param title The pane title.
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build(final String title) {
        return infoPane
                .setGridStyleId("component-grid")
                .setTitle(title)
                .build();
    }

    /**
     * Bind the property map to the info pane.
     *
     * @param propertyMap The properties stored in the info pane.
     */
    public void bindIntegers(final Map<String, IntegerProperty> propertyMap) {
        infoPane.bindIntegers(propertyMap);
    }

    /**
     * Bind the property map to the info pane.
     *
     * @param propertyMap The property map stored in the info pane.
     */
    public void bindStrings(final Map<String, StringProperty> propertyMap) {
        infoPane.bindStrings(propertyMap);
    }
}
