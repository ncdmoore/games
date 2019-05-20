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
     * Mine one of the available zone map reference grids.
     *
     * @param grid The map reference grid that is mined.
     */
    public void addMine(final String grid) {
        if (hasRoom()) {
            activeMapRef.add(grid);
            log.info("Add mine to grid: '{}' in minefield: '{}'", grid, zoneName);
        }
    }

    /**
     * Remove a mine from the minefield. The given grid is no longer mined.
     *
     * @param grid The map reference grid that has the mines removed.
     */
    public void removeMine(final String grid) {
        log.info("Remove mine from grid: '{}' in minefield: '{}'", grid, zoneName);
        activeMapRef.remove(grid);
    }

    /**
     * Indicates that the minefield may have another grid mined.
     *
     * @return True if the minefield has room for another mined grid.
     */
    public boolean hasRoom() {
        return activeMapRef.size() < number;
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
