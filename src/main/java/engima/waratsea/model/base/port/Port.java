package engima.waratsea.model.base.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.port.data.PortData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.Location;
import engima.waratsea.model.map.LocationFactory;
import lombok.Getter;

/**
 * Represents a port within the game.
 */
public class Port implements Base, PersistentData<PortData> {
    @Getter
    private final Side side;

    @Getter
    private final String name;

    @Getter
    private final String size;

    @Getter
    private final int antiAir;

    @Getter
    private final Location location;

    /**
     * Constructor called by guice.
     * @param side The side of the port ALLIES or AXIS.
     * @param data The port data read in from a JSON file.
     * @param factory The location factory.
     */
    @Inject
    public Port(@Assisted final Side side,
                @Assisted final PortData data,
                          final LocationFactory factory) {
        this.side = side;
        name = data.getName();
        size = data.getSize();
        antiAir = data.getAntiAir();
        location = factory.create(data.getLocation());
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
       data.setLocation(location.getReference());
       return data;
    }

    /**
     * Get the port's map reference.
     *
     * @return The port's map reference.
     */
    @Override
    public String getReference() {
        return location.getReference();
    }
}
