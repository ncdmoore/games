package engima.waratsea.view;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.utility.PropertyWrapper;

/**
 * This class represents all the view properties.
 */
@Singleton
public class ViewProps {

    private final PropertyWrapper props;

    private static final String VIEW_PROPERTIES = "properties/view.properties";

    /**
     * The constructor of the View properties.  First, the default view properties are loaded and then the game specific
     *      * view properties are loaded. This way a game specific view property may overwrite a default application view
     *      * property.
     * @param gameTitle The game'title/name.
     * @param props Property wrapper.
     */
    @Inject
    public ViewProps(final GameTitle gameTitle,
                     final PropertyWrapper props) {
        this.props = props;

        String gameName = gameTitle.getValue();

        props.init(VIEW_PROPERTIES);                                                                                    // Load default view properties.
        props.init(gameName + "/" + VIEW_PROPERTIES);                                                            // Load game specific properties.
    }

    /**
     * Get a string property.
     * @param key The name of the property to getShipData.
     * @return The health of the property.
     */
    public String getString(final String key) {
        return props.getString(key);
    }

    /**
     * Get an integer property.
     * @param key The name of the property to getShipData.
     * @return The health of the property.
     */
    public int getInt(final String key) {
        return props.getInt(key);
    }

    /**
     * Get a double property.
     * @param key The name of the property to getShipData.
     * @return The health of the property.
     */
    public double getDouble(final String key) {
        return props.getDouble(key);
    }
}
