package engima.waratsea.view.taskforce.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import javafx.beans.property.IntegerProperty;

import java.util.Map;

public class TaskForceInfo {
    private final ViewProps props;

    private final BoundTitledGridPane infoPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public TaskForceInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the pane node.
     *
     * @param title The pane's title.
     * @return A node that contains the info pane.
     */
    public BoundTitledGridPane build(final String title) {
        return infoPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle(title)
                .build();
    }

    /**
     * Bind the properties to the pane.
     *
     * @param propertyMap Contains the values displayed in the info pane.
     */
    public void bind(final Map<String, IntegerProperty> propertyMap) {
        infoPane.bindIntegers(propertyMap);
    }
}
