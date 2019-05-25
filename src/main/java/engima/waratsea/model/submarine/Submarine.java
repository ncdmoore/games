package engima.waratsea.model.submarine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.PersistentData;
import engima.waratsea.model.flotilla.Flotilla;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.ship.ShipId;
import engima.waratsea.model.ship.ShipType;
import engima.waratsea.model.ship.Torpedo;
import engima.waratsea.model.submarine.data.SubmarineData;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a submarine.
 */
public class Submarine implements PersistentData<SubmarineData> {
    @Getter
    private final ShipId shipId;

    @Getter
    private final ShipType type;

    @Getter
    private final String shipClass;

    @Getter
    private final Nation nationality;

    @Getter
    private final int victoryPoints;

    @Getter
    private final Torpedo torpedo;

    @Getter
    @Setter
    private Flotilla flotilla;

    /**
     * Constructor called by guice.
     *
     * @param data The submarine data read in from a JSON file.
     */
    @Inject
    public Submarine(@Assisted final SubmarineData data) {
        this.shipId = data.getShipId();
        this.type = data.getType();
        this.shipClass = data.getShipClass();
        this.nationality = data.getNationality();
        this.victoryPoints = data.getVictoryPoints();

        torpedo = new Torpedo(data.getTorpedo());
    }

    /**
     * Get the persistent submarine data.
     *
     * @return Return the persistent submarine data.
     */
    public SubmarineData getData() {
        SubmarineData data = new SubmarineData();
        data.setShipId(shipId);
        data.setType(type);
        data.setShipClass(shipClass);
        data.setNationality(nationality);
        data.setVictoryPoints(victoryPoints);
        data.setTorpedo(torpedo.getData());
        return data;
    }
}
