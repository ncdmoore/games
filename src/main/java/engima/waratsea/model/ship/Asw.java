package engima.waratsea.model.ship;

import engima.waratsea.model.ship.data.AswData;
import lombok.Getter;
import lombok.Setter;

/**
 * Models the Anti Submarine capability of the ship. Data is read in from a JSON file.
 */
public class Asw {
    @Getter
    @Setter
    private boolean asw;

    /**
     * Constructor.
     *
     * @param data The ASW data read in from a JSON file.
     */
    public Asw(final AswData data) {
        asw = data.isCapable();
    }

    /**
     * Get the ASW data that is persistent.
     *
     * @return ASW persistent data.
     */
    AswData getData() {
        AswData data = new AswData();
        data.setCapable(asw);
        return data;
    }
}
