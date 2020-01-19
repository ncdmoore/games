package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TargetFriendlyPort implements Target {

    private final Game game;
    private final GameMap gameMap;

    @Getter
    private final String name;

    private final Side side;

    //private int priority;`

    private Port port;

    /**
     * Constructor called by guice.
     *
     * @param data The target data read in from a JSON file.
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public TargetFriendlyPort(@Assisted final TargetData data,
                                        final Game game,
                                        final GameMap gameMap) {
        this.game = game;
        this.gameMap = gameMap;

        name = data.getName();
        side = data.getSide();
    }

    /**
     * Get the location of the target.
     *
     * @return The target's location.
     */
    @Override
    public String getLocation() {
        return Optional
                .ofNullable(port)
                .orElseGet(this::getPort)
                .getReference();
    }

    /**
     * Get the target data that is persisted.
     *
     * @return The persistent target data.
     */
    @Override
    public TargetData getData() {
        TargetData data = new TargetData();
        data.setName(name);
        data.setType(TargetType.FRIENDLY_PORT);
        data.setSide(side);
        return data;
    }

    /**
     * The String representation of this target.
     *
     * @return The String representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Determine if this squadron is in range of the given squadron.
     *
     * @param squadron The squadron that is determined to be in or out of range of this target.
     * @return True if this target is in range of the given squadron. False otherwise.
     */
    @Override
    public boolean inRange(final Squadron squadron) {
        String targetReference = gameMap.convertNameToReference(getLocation());

        GameGrid targetGrid = gameMap.getGrid(targetReference);
        GameGrid airbaseGrid = gameMap.getGrid(squadron.getAirfield().getReference());

        // a^2 + b^2 <= c^2, where a, b and c are the sides of the right triangle.
        int a = Math.abs(targetGrid.getRow() - airbaseGrid.getRow());
        int b = Math.abs(targetGrid.getColumn() - airbaseGrid.getColumn());

        int c = squadron.getMaxRadius() + 1;

        log.info("a: {} ,b: {}, c: {}", new Object[]{a, b, c});

        return (a * a) + (b * b) <= (c * c);
    }

    /**
     * Save any of this object's children persistent data.
     * Not all objects will have children with persistent data.
     */
    @Override
    public void saveChildrenData() {
    }

    /**
     * Get the port view for this target.
     *
     * @return This target's port view.
     */
    private Port getPort() {
        port = game.getPlayer(side)
                .getPortMap()
                .get(name);

        if (port == null) {
            log.error("Cannot find port: '{}' for side: '{}'", name, side);
        }

        return port;
    }

}
