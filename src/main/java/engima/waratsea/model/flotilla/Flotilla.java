package engima.waratsea.model.flotilla;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.flotilla.data.FlotillaData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipyardException;
import engima.waratsea.model.submarine.Submarine;
import engima.waratsea.model.submarine.SubmarineDAO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a flotilla.
 */
@Slf4j
public class Flotilla implements PersistentData<FlotillaData> {
    @Getter
    @Setter
    private String name;

    @Getter
    private final Side side;

    @Getter
    @Setter
    private List<Submarine> subs;

    @Getter
    @Setter
    private String location; //This is always a map reference and never a name.

    private SubmarineDAO submarineDAO;

    /**
     * Constructor called by guice.
     *
     * @param side The side ALLIES or AXIS.
     * @param data The flotilla data read in from a JSON file.
     * @param submarineDAO Loads and saves persistent submarine data.
     */
    @Inject
    public Flotilla(@Assisted final Side side,
                    @Assisted final FlotillaData data,
                              final SubmarineDAO submarineDAO) {
        this.name = data.getName();
        this.location = data.getLocation();
        this.side = side;

        this.submarineDAO = submarineDAO;

        buildSubs(data.getSubs());
    }

    /**
     * Save the flotilla sub data.
     */
    public void saveSubs() {
        subs.forEach(submarine -> submarineDAO.save(submarine));
    }

    /**
     * Get the persistent data.
     *
     * @return The persistent data.
     */
    @Override
    public FlotillaData getData() {
        FlotillaData data = new FlotillaData();
        data.setName(name);
        data.setLocation(location);
        data.setSubs(getSubNames(subs));
        return data;
    }

    /**
     * The String representation of the flotilla.
     *
     * @return The String representation of the flotilla.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Build all the flotilla's submarines.
     *
     * @param subNames list of sub names.
     */
    private void buildSubs(final List<String> subNames) {
        subs = subNames.stream()
                .map(subName -> new ShipId(subName, side))
                .map(this::buildSub)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Build a given ship.
     *
     * @param shipId Uniquely identifies a ship.
     * @return The constructed ship.
     */
    private Submarine buildSub(final ShipId shipId) {
        try {
            Submarine sub = submarineDAO.load(shipId);
            sub.setFlotilla(this);
            return sub;
        } catch (ShipyardException ex) {
            log.error("Unable to build sub '{}' for side {}", shipId.getName(), shipId.getSide());
            return null;
        }
    }

    /**
     * Get a list of submarine names.
     *
     * @param submarines A list of submarine objects.
     * @return A list of submarine names.
     */
    private List<String> getSubNames(final List<Submarine> submarines) {
        return submarines
                .stream()
                .map(submarine -> submarine.getShipId().getName())
                .collect(Collectors.toList());
    }
}
