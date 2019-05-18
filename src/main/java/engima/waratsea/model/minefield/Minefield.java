package engima.waratsea.model.minefield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.data.MinefieldData;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a minefield.
 */
@Slf4j
public class Minefield implements PersistentData<MinefieldData> {
    @Getter
    private final String zoneName;

    @Getter
    private final Side side;

    @Getter
    @Setter
    private MinefieldZone zone;

    @Getter
    private final int number;

    @Getter
    @Setter
    private List<String> activeMapRef = new ArrayList<>();

    /**
     * Constructor called by guice.
     *
     * @param data The minefield data read in from a JSON file.
     */
    @Inject
    public Minefield(@Assisted final MinefieldData data) {
        this.zoneName = data.getZone();
        this.side = data.getSide();
        this.number = data.getNumber();
    }

    /**
     * Get the minefield persistent data.
     *
     * @return The minefield persistent data.
     */
    public MinefieldData getData() {
        MinefieldData data = new MinefieldData();
        data.setZone(zoneName);
        data.setSide(side);
        data.setNumber(number);
        data.setActiveMapRef(activeMapRef);
        return data;
    }

    /**
     * Get a list of map references that make up the minefield zone.
     *
     * @return A list of map references.
     */
    public List<String> getZoneMapRefs() {
        return zone.getGrids();
    }

    /**
     * Mine one of the available zone map reference grids.
     *
     * @param grid The map reference grid that is mined.
     */
    public void mineGrid(final String grid) {
        if (activeMapRef.size() < number) {
            activeMapRef.add(grid);
            log.info("Mine grid: '{}' in minefield: '{}'", grid, zoneName);
        }
    }

    /**
     * Get the String representation of the minefield.
     *
     * @return The String representation, the minefield zone name.
     */
    @Override
    public String toString() {
        return zoneName;
    }
}
