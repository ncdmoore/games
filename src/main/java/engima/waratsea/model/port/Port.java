package engima.waratsea.model.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.port.data.PortData;
import lombok.Getter;

/**
 * Represents a port within the game.
 */
public class Port {

    @Getter
    private final String name;

    @Getter
    private final Side side;

    @Getter
    private final String size;

    private String location;

    /**
     * Constructor called by guice.
     * @param side The side of the port ALLIES or AXIS.
     * @param data The port data read in from a JSON file.
     */
    @Inject
    public Port(@Assisted final Side side,
                @Assisted final PortData data) {
        this.side = side;
        name = data.getName();
        size = data.getSize();
    }

}
