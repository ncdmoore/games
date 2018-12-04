package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.ProperyWrapper;

/**
 * This class represents all the view properties.
 */
@Singleton
public class ViewProps {

    private final ProperyWrapper props;

    private static final String VIEW_PROPERTIES = "properties/view.properties";

    /**
     * The constructor of the View properties.
     *
      * @param props Property wrapper.
     */
    @Inject
    public ViewProps(final ProperyWrapper props) {
        this.props = props;
    }

    /**
     * Initialize the view properties. First, the default view properties are loaded and then the game specific
     * view properties are loaded. This way a game specific view property may overwrite a default application view
     * property.
     *
     * @param name Specifies the name of the game which then is used to determine which properties file to load.
     */
    public void init(final String name) {
        props.init(VIEW_PROPERTIES);                                                                                    // Load default view properties.
        props.init(name + "/" + VIEW_PROPERTIES);                                                                 // Load game specific properties.
    }

    /**
     * Get a string property.
     * @param key The name of the property to get.
     * @return The value of the property.
     */
    public String getString(final String key) {
        return props.getString(key);
    }

    /**
     * Get an integer property.
     * @param key The name of the property to get.
     * @return The value of the property.
     */
    public int getInt(final String key) {
        return props.getInt(key);
    }
}
