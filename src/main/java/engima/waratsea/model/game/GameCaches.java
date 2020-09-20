package engima.waratsea.model.game;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.airfield.AirfieldDAO;
import engima.waratsea.model.base.port.PortDAO;
import engima.waratsea.model.game.event.GameEvent;
import engima.waratsea.model.target.TargetDAO;

/**
 * This class manages any classes within the game caches.
 *
 * Note, it does not contain any cache's itself.
 */
@Singleton
public class GameCaches {

    private final AirfieldDAO airfieldDAO;
    private final PortDAO portDAO;
    private final TargetDAO targetDAO;

    /**
     * Constructor called by guice.
     *
     * @param airfieldDAO The airfield Data access object. Provides airfields.
     * @param portDAO The port access object. Provides ports.
     * @param targetDAO The target access object. Provides targets.
     */
    @Inject
    public GameCaches(final AirfieldDAO airfieldDAO,
                      final PortDAO portDAO,
                      final TargetDAO targetDAO) {
        this.airfieldDAO = airfieldDAO;
        this.portDAO = portDAO;
        this.targetDAO = targetDAO;
    }

    /**
     * Initialize the game caches.
     */
    public void init() {
        GameEvent.init();

        airfieldDAO.init();
        portDAO.init();
        targetDAO.init();
    }
}
