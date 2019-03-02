package engima.waratsea.model.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.port.data.PortData;
import lombok.Getter;

/**
 * Represents a port within the game.
 */
public class Port implements PersistentData<PortData> {
    @Getter
    private final Side side;

    @Getter
    private final String name;

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


    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public PortData getData() {
       PortData data = new PortData();
       data.setName(name);
       data.setSize(size);
       return data;
    }
}
