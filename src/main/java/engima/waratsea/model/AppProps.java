package engima.waratsea.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.ProperyWrapper;

/**
 * This class represents all the view properties.
 */
@Singleton
public class AppProps {

    private final ProperyWrapper props;

    private static final String APP_PROPERTIES = "properties/app.properties";

    /**
     * The constructor of the Application properties.
     *
     * @param props Property wrapper.
     */
    @Inject
    public AppProps(final ProperyWrapper props) {
        this.props = props;
    }

    /**
     * Initialize the application properties. First, the default application properties are loaded and then the game
     * specific application properties are loaded. This way a game specific property may overwrite a default application
     * property.
     *
     * @param name Specifies the name of the game which then is used to determine which properties file to load.
     */
    public void init(final String name) {
        props.init(APP_PROPERTIES);                                                                                    // Load default view properties.
        props.init(name + "/" + APP_PROPERTIES);                                                                 // Load game specific properties.
    }

    /**
     * Get a string property.
     * @param key The name of the property to get.
     * @return The property value.
     */
    public String getString(final String key) {
        return props.getString(key);
    }

    /**
     * Get an integer property.
     * @param key The name of the property to get.
     * @return The property value.
     */
    public int getInt(final String key) {
        return props.getInt(key);
    }
}
