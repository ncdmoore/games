package engima.waratsea.model.game;

/**
 * Global game configuration parameters.
 */
public final class Config {
    public static final String MAP_DIRECTORY_NAME = "/maps";
    public static final String SCENARIO_DIRECTORY_NAME = "/scenarios";
    public static final String AIRFIELD_DIRECTORY_NAME = "/airfields";
    public static final String PORT_DIRECTORY_NAME = "/ports";
    public static final String VICTORY_DIRECTORY_NAME = "victory";

    public static final String SAVED_GAME_DIRECTORY = System.getProperty("user.home") + "/WW2atSea/SavedGames/";


    /**
     * This class is never constructecd.
     */
    private Config() {
    }
}
