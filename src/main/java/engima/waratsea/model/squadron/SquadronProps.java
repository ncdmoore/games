package engima.waratsea.model.squadron;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.utility.ProperyWrapper;

/**
 * This class represents all the squadron properties.
 */
@Singleton
public class SquadronProps {

    private final ProperyWrapper props;

    private static final String SQUADRON_PROPERTIES = "/squadrons/allotment/squadron.properties";


    /**
     * The constructor of the squadron properties called by guice.
     *
     * @param gameTitle The game's title/name.
     * @param props Property wrapper.
     */
    @Inject
    public SquadronProps(final GameTitle gameTitle,
                         final ProperyWrapper props) {
        this.props = props;

        String gameName = gameTitle.getValue();

        props.init(gameName + SQUADRON_PROPERTIES);                                                                    // Load game specific properties.
    }

    /**
     * Get a string property.
     *
     * @param key The name of the property to get.
     * @return The property value.
     */
    public String getString(final String key) {
        return props.getString(key);
    }

    /**
     * Get an integer property.
     *
     * @param key The name of the property to get.
     * @return The property value.
     */
    public int getInt(final String key) {
        return props.getInt(key);
    }
}
