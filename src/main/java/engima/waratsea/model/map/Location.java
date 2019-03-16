package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents a location on the game map.
 */
@Slf4j
public class Location {
    //private LocationType locationType;

    @Getter
    private final String reference; // This is mandatory.

    @Getter
    private final String name; // This is optional. A location will only have a name if it maps to a base.

    private GameMap gameMap;

    /**
     * The constructor called by guice.
     *
     * @param name The name of the location. May be an airfield or port name. May also simply be a map reference.
     * @param gameMap The game's map.
     */
    @Inject
    public Location(@Assisted final String name,
                              final GameMap gameMap) {
        this.name = name;
        this.gameMap = gameMap;
        this.reference = gameMap.convertNameToReference(name);
    }

    /**
     * Determine if this location is an enemy base.
     *
     * @param side The side ALLIES or AXIS.
     * @return True if this location is an enemy base. False otherwise.
     */
    public boolean isEnemyBase(final Side side) {
        return gameMap.isLocationBase(side.opposite(), reference);
    }

    /**
     * Determine if this location is a friendly base.
     *
     * @param side The side ALLIES or AXIS.
     * @return True if this location is a friendly base. False otherwise.
     */
    public boolean isFriendlyBase(final Side side) {
        return gameMap.isLocationBase(side, reference);
    }

    /**
     * The string representation of this class.
     *
     * @return The location's name is returned.
     */
    @Override
    public String toString() {
        return name;
    }
}
