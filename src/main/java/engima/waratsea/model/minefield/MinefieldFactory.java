package engima.waratsea.model.minefield;

import engima.waratsea.model.minefield.data.MinefieldData;

/**
 * Create's minefield objects.
 */
public interface MinefieldFactory {
    /**
     * Create a minefield object.
     *
     * @param data The minefield data read in from a JSON file.
     * @return The minefield initialized with the data read in from a JSON file.
     */
    Minefield create(MinefieldData data);
}
