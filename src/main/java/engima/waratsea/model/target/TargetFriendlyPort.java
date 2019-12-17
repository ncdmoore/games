package engima.waratsea.model.target;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.target.data.TargetData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class TargetFriendlyPort implements Target {

    private final Game game;

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
     */
    @Inject
    public TargetFriendlyPort(@Assisted final TargetData data,
                              final Game game) {
        this.game = game;

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
            log.error("Cannot found port: '{}' for side: '{}'", name, side);
        }

        return port;
    }

}
