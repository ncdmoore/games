package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
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
     * @param gameTitle The game's title/name.
     * @param props Property wrapper.
     */
    @Inject
    public MapProps(final GameTitle gameTitle,
                    final ProperyWrapper props) {
        this.props = props;

        String gameName = gameTitle.getValue();

        props.init(gameName + "/" + MAP_PROPERTIES);                                                                 // Load game specific properties.

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
