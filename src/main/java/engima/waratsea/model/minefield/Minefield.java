package engima.waratsea.model.minefield;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.minefield.data.MinefieldData;
import engima.waratsea.model.minefield.zone.MinefieldZone;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a minefield.
 */
public class Minefield {
    @Getter
    private final String name;

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
        this.name = data.getZone();
        this.side = data.getSide();
        this.number = data.getNumber();
    }

    /**
     * Get a list of map references that make up the minefield zone.
     *
     * @return A list of map references.
     */
    public List<String> getZoneMapRefs() {
        return zone.getReferences();
    }

    /**
     * Mine one of the available zone map reference grids.
     *
     * @param mapRef The map reference grid that is mined.
     */
    public void mineMapRef(final String mapRef) {
        if (activeMapRef.size() < number) {
            activeMapRef.add(mapRef);
        }
    }
}
