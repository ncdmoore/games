package engima.waratsea.model.submarine;

import engima.waratsea.model.submarine.data.SubmarineData;

/**
 * Creates submarines.
 */
public interface SubmarineFactory {
    /**
     * Creates a submarine.
     *
     * @param data The submarine's data.
     * @return The submarine.
     */
    Submarine create(SubmarineData data);
}
