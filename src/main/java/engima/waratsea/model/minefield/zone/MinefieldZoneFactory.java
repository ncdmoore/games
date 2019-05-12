package engima.waratsea.model.minefield.zone;

import engima.waratsea.model.minefield.zone.data.MinefieldZoneData;

/**
 * Create's minefield objects.
 */
public interface MinefieldZoneFactory {
    /**
     * Create a minefield object.
     *
     * @param data The minefield data read in from a JSON file.
     * @return A minefield zone.
     */
    MinefieldZone create(MinefieldZoneData data);
}
