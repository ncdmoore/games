package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.game.event.GameEvent;

/**
 * This class manages any classes within the game caches.
 *
 * Note, it does not contain any cache's itself.
 */
@Singleton
public class GameCaches {

    private AirfieldDAO airfieldDAO;
    private PortDAO portDAO;

    /**
     * Constructor called by guice.
     *
     * @param airfieldDAO The airfield Data access object. Provides airfields.
     * @param portDAO The port access object. Provides ports.
     */
    @Inject
    public GameCaches(final AirfieldDAO airfieldDAO, final PortDAO portDAO) {
        this.airfieldDAO = airfieldDAO;
        this.portDAO = portDAO;
    }

    /**
     * Initialize the game caches.
     */
    public void init() {
        GameEvent.init();

        airfieldDAO.init();
        portDAO.init();
    }
}
