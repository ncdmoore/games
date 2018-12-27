package engima.waratsea.model;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.utility.ProperyWrapper;

/**
 * This class represents all the application properties.
 */
@Singleton
public class AppProps {

    private final ProperyWrapper props;
    private static final String APP_PROPERTIES = "properties/app.properties";

    /**
     * The constructor of the Application properties. First, the default application properties are loaded and then the game
     *      * specific application properties are loaded. This way a game specific property may overwrite a default application
     *      * property.
     * @param gameTitle The game's title/name.
     * @param props Property wrapper.
     */
    @Inject
    public AppProps(final GameTitle gameTitle, final ProperyWrapper props) {
        String gameName = gameTitle.getValue();

        props.init(APP_PROPERTIES);                                                                                    // Load default application properties.
        props.init(gameName + "/" + APP_PROPERTIES);                                                             // Load game specific properties.

        this.props = props;
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
