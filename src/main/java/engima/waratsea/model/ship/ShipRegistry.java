package engima.waratsea.model.ship;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.utility.PropertyWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps ship names to ship class. This class's main purpose is to return a ship class name given a ship name.
 */
@Singleton
@Slf4j
public class ShipRegistry {
    private static final String ALLIED_SHIP_PROPERTIES = "ships/allies/alliedShip.properties";
    private static final String AXIS_SHIP_PROPERTIES = "ships/axis/axisShip.properties";

    private static  Map<Side, PropertyWrapper> shipMap = new HashMap<>();

    /**
     * The constructor of the Ship registry.
     *
     * @param gameTitle The game's title/name.
     * @param alliedShips Allied ships properties.
     * @param axisShips Axis ships properties.
     */
    @Inject
    public ShipRegistry(final GameTitle gameTitle,
                        final PropertyWrapper alliedShips,
                        final PropertyWrapper axisShips) {
        shipMap.put(Side.ALLIES, alliedShips);
        shipMap.put(Side.AXIS, axisShips);

        String gameName = gameTitle.getValue();

        shipMap.get(Side.ALLIES).init(gameName + "/" + ALLIED_SHIP_PROPERTIES);                                      // Load game specific allied ship properties.
        shipMap.get(Side.AXIS).init(gameName + "/" + AXIS_SHIP_PROPERTIES);                                          // Load game specific axis ship properties.
    }

    /**
     * Get a ship's class given the ship's name.
     *
     * @param shipId Uniquely identifies a ship.
     * @return The ship class.
     */
    public String getClass(final ShipId shipId) {
        String shipName = shipId.getName();
        Side side = shipId.getSide();
        String shipClassName = shipMap.get(side).getString(shipName.trim());
        log.debug("For ship '{}' and side {} get class '{}'", new Object[]{shipName, side, shipClassName});
        return shipClassName;
    }
}
