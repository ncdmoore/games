package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents airfield's in the game.
 */
public class Airfield implements Base, PersistentData<AirfieldData> {

    @Getter
    private final Side side;

    @Getter
    private final String name;

    @Getter
    private final int maxCapacity;   //Capacity in steps.

    @Getter
    private final int antiAir;

    private final String location; // A simple string is used to prevent circular logic on mapping names and references.

    @Getter
    @Setter
    private int capacity;            //Capacity in steps.

    /**
     * Constructor called by guice.
     *
     * @param side The side of the airfield ALLIES or AXIS.
     * @param data The airfield data read in from a JSON file.
     */
    @Inject
    public Airfield(@Assisted final Side side,
                    @Assisted final AirfieldData data) {
        this.side = side;
        name = data.getName();
        maxCapacity = data.getMaxCapacity();
        capacity = maxCapacity;
        antiAir = data.getAntiAir();
        location = data.getLocation();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public AirfieldData getData() {
        AirfieldData data = new AirfieldData();
        data.setName(name);
        data.setMaxCapacity(maxCapacity);
        data.setCapacity(capacity);
        data.setAntiAir(antiAir);
        data.setLocation(location);
        return data;
    }

    /**
     * Get the airfield's location map reference.
     *
     * @return The airfield's map reference.
     */
    @Override
    public String getReference() {
        return location;
    }
}
