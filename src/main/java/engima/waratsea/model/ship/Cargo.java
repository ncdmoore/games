package engima.waratsea.model.ship;

import engima.waratsea.model.map.Location;
import engima.waratsea.model.ship.data.CargoData;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * A ship's cargo.
 */
public class Cargo {
    @Getter
    private final int capacity; // The ship's total cargo capacity. How much cargo a ship can hold.

    @Getter
    @Setter
    private int level; // The current amount of cargo in the ship's holds.

    @Getter
    @Setter
    private Location originPort;

    /**
     * Constructor.
     *
     * @param data The cargo capacity of the ship.
     */
    public Cargo(final CargoData data) {
        this.capacity = data.getCapacity();
        this.level = 0;
    }

    /**
     * Get the cargo's persistent data.
     *
     * @return The cargo's persistent data.
     */
    public CargoData getData() {
        CargoData data = new CargoData();
        data.setCapacity(capacity);
        data.setLevel(level);

        Optional.ofNullable(originPort).ifPresent(port -> data.setOriginPort(port.getName()));

        return data;
    }

    /**
     * Load all of the cargoShips.
     *
     * @param port The port where the cargo is loaded.
     */
    public void load(final Location port) {
        level = capacity;
        originPort = port;
    }
}
