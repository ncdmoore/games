package engima.waratsea.view.airfield.info;

import com.google.inject.Inject;
import engima.waratsea.view.ViewProps;
import engima.waratsea.view.util.BoundTitledGridPane;
import javafx.beans.property.IntegerProperty;

import java.util.Map;

public class AirfieldInfo {
    private final ViewProps props;

    private final BoundTitledGridPane infoPane = new BoundTitledGridPane();

    /**
     * Constructor called by guice.
     *
     * @param props The view properties.
     */
    @Inject
    public AirfieldInfo(final ViewProps props) {
        this.props = props;
    }

    /**
     * Build the squadron counts node.
     *
     * @param title The pane title.
     * @return A node that contains the squadron counts for each type of aircraft.
     */
    public BoundTitledGridPane build(final String title) {
        return infoPane
                .setWidth(props.getInt("airfield.dialog.airfield.details.width"))
                .setGridStyleId("component-grid")
                .setTitle(title)
                .build();
    }

    /**
     * Set the airbase.
     *
     * @param propertyMap The properties stored in the info pane.
     */
    public void bind(final Map<String, IntegerProperty> propertyMap) {
        infoPane.bindIntegers(propertyMap);
    }
}
