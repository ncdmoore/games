package engima.waratsea.model.minefield.zone;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.minefield.zone.data.MinefieldZoneData;
import lombok.Getter;

import java.util.List;

/**
 * Represents a minefield.
 */
public class MinefieldZone {
    @Getter
    private final String name;

    @Getter
    private final List<String> references;

    /**
     * Constructor called by guice.
     *
     * @param data The minefield data read in from a JSON file.
     */
    @Inject
    public MinefieldZone(@Assisted final MinefieldZoneData data) {
        this.name = data.getName();
        this.references = data.getGrids();
    }
}
