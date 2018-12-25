package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.utility.ProperyWrapper;

/**
 * This class represents all the map properties.
 */
@Singleton
public class MapProps {

    private final ProperyWrapper props;

    private static final String MAP_PROPERTIES = "properties/map.properties";

    /**
     * The constructor of the Map properties.
     *
     * @param props Property wrapper.
     */
    @Inject
    public MapProps(final ProperyWrapper props) {
        this.props = props;
    }

    /**
     * Initialize the game map properties.
     *
     * @param name Specifies the name of the game which then is used to determine which properties file to load.
     */
    public void init(final String name) {
        props.init(name + "/" + MAP_PROPERTIES);                                                                 // Load game specific properties.
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
