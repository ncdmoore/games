package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.data.AirfieldData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents airfield's in the game.
 */
@Slf4j
public class Airfield implements Airbase, PersistentData<AirfieldData> {

    @Getter
    private final Side side;

    @Getter
    private final String name;

    @Getter
    private final int maxCapacity;   //Capacity in steps.

    @Getter
    private final int antiAir;

    @Getter
    private final String reference; // A simple string is used to prevent circular logic on mapping names and references.
                                    // Airfields are used to map airfield names to map references. Thus, we just need a map reference
    @Getter
    @Setter
    private int capacity;           //Capacity in steps.

    @Getter
    @Setter
    private  Region region;

    @Getter
    private List<Squadron> squadrons = new ArrayList<>();

    /**
     * Constructor called by guice.
     *
     * @param data The airfield data read in from a JSON file.
     */
    @Inject
    public Airfield(@Assisted final AirfieldData data) {
        this.side = data.getSide();
        name = data.getName();
        maxCapacity = data.getMaxCapacity();
        capacity = maxCapacity;
        antiAir = data.getAntiAir();
        reference = data.getLocation();
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public AirfieldData getData() {
        AirfieldData data = new AirfieldData();
        data.setSide(side);
        data.setName(name);
        data.setMaxCapacity(maxCapacity);
        data.setCapacity(capacity);
        data.setAntiAir(antiAir);
        data.setLocation(reference);
        return data;
    }

    /**
     * Get the nations that may operate aircraft from this airfield.
     *
     * @return A list of nations that may operate aircraft from this airfield.
     */
    public List<Nation> getNations() {
        return region.getNations();
    }

    /**
     * Base a squadron from this airfield.
     *
     * @param squadron The squadron which is now based at this airfield.
     */
    @Override
    public boolean addSquadron(final Squadron squadron) {

        if (hasRoom(squadron)) {
            Optional.ofNullable(squadron.getAirfield())
                    .ifPresent(airfield -> airfield.removeSquadron(squadron));
            squadrons.add(squadron);
            squadron.setAirfield(this);
            return true;
        }

        return false;
    }

    /**
     * Remove a squadron from this airfield.
     *
     * @param squadron The squadron which is removed from this airfield.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
        squadron.setAirfield(null);
    }

    /**
     * Determine if this airfield has room for another squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this airfield can house the new squadron; false otherwise.
     */
    private boolean hasRoom(final Squadron squadron) {
        return determineRoom(squadron) && region.hasRoom(squadron);
    }

    /**
     * Determine if this airfield has room for the given squadron.
     *
     * @param squadron A potential new squadron.
     * @return True if this arifield can house the new squadron. False otherwise.
     */
    private boolean determineRoom(final Squadron squadron) {
        int steps = squadron.getSteps().intValue();

        boolean result = steps + deployedSteps() <= maxCapacity;

        log.debug("Airfield: '{}' has room result: {}", name, result);

        if (!result) {
            log.warn("Airfield: '{}' has max capacity: {} and current capacity: {}", new Object[]{name, maxCapacity, deployedSteps()});
        }

        return result;
    }

    /**
     * Determine the current number of steps deployed at this airfield.
     *
     * @return The current number of steps deployed at this airfield.
     */
    private int deployedSteps() {
        return squadrons
                .stream()
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .intValue();
    }
}
