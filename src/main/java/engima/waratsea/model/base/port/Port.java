package engima.waratsea.model.base.port;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.port.data.PortData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import lombok.Getter;
import lombok.Setter;

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
    private final String reference; // A simple string is used to prevent circular logic on mapping names and references.
                                    // Ports are used to map port names to map references. Thus, we just need a map reference.

    @Getter
    @Setter
    private Region region;

    /**
     * Constructor called by guice.
     *
     * @param data The port data read in from a JSON file.
     */
    @Inject
    public Port(@Assisted final PortData data) {
        side = data.getSide();
        name = data.getName();
        size = data.getSize();
        antiAir = data.getAntiAir();
        reference = data.getLocation();
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
       data.setLocation(reference);
       return data;
    }
}
