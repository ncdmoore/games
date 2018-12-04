package engima.waratsea.model.game;

/**
 * WW2 at sea contains two combatants or sides.
 */
public enum Side {

    ALLIES,
    AXIS;

    /**
     * Given a side this method returns the opposing side.
     * @return The opposing side is returned.
     */
    public Side opposite() {
        return this == ALLIES ? AXIS : ALLIES;
    }
}
