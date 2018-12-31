package engima.waratsea.model.ships;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.game.GameTitle;
import engima.waratsea.model.game.Side;
import engima.waratsea.utility.ProperyWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps ship names to ship class.
 */
@Singleton
@Slf4j
public class ShipRegistry {
    private static final String ALLIED_SHIP_PROPERTIES = "ships/allies/alliedShip.properties";
    private static final String AXIS_SHIP_PROPERTIES = "ships/axis/axisShip.properties";

    private static  Map<Side, ProperyWrapper> shipMap = new HashMap<>();

    /**
     * The constructor of the Ship registry.
     * @param gameTitle The game's title/name.
     * @param alliedShips Allied ships properties.
     * @param axisShips Axis ships properties.
     */
    @Inject
    public ShipRegistry(final GameTitle gameTitle,
                        final ProperyWrapper alliedShips,
                        final ProperyWrapper axisShips) {
        shipMap.put(Side.ALLIES, alliedShips);
        shipMap.put(Side.AXIS, axisShips);

        String gameName = gameTitle.getValue();

        shipMap.get(Side.ALLIES).init(gameName + "/" + ALLIED_SHIP_PROPERTIES);                                      // Load game specific allied ship properties.
        shipMap.get(Side.AXIS).init(gameName + "/" + AXIS_SHIP_PROPERTIES);                                          // Load game specific axis ship properties.
    }

    /**
     * Get a ship's class given the ship's name.
     * @param side The side ALLIED or AXIS.
     * @param shipName The ship name.
     * @return The ship class.
     */
    public String getClass(final Side side, final String shipName) {
        String shipClassName = shipMap.get(side).getString(shipName.trim());
        log.info("For ship '{}' and side {} get class '{}'", new Object[]{shipName, side, shipClassName});
        return shipClassName;
    }
}
