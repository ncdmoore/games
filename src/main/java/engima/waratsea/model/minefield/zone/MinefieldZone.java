package engima.waratsea.model.minefield.zone;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.minefield.zone.data.MinefieldZoneData;
import lombok.Getter;

import java.util.List;

/**
 * Represents a minefield zone. A zone is just the collection of available grids. Grids that may be mined.
 */
public class MinefieldZone {
    @Getter
    private final String name;

    @Getter
    private final List<String> grids;

    /**
     * Constructor called by guice.
     *
     * @param data The minefield data read in from a JSON file.
     */
    @Inject
    public MinefieldZone(@Assisted final MinefieldZoneData data) {
        this.name = data.getName();
        this.grids = data.getGrids();
    }

    /**
     * Get the minefield zone persistent data.
     *
     * @return The minefield zone persistent data.
     */
    public MinefieldZoneData getData() {
        MinefieldZoneData data = new MinefieldZoneData();
        data.setName(name);
        data.setGrids(grids);
        return data;
    }
}
