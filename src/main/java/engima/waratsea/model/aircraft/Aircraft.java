package engima.waratsea.model.aircraft;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;

/**
 * Represents an aircraft.
 */
public interface Aircraft {

    /**
     * Get the aircraft's model.
     *
     * @return The aircraft's model.
     */
    String getModel();


    /**
     * Get the aircraft's side.
     *
     * @return The aircraft's side.
     */
    Side getSide();

    /**
     * Get the aircraft's type.
     *
     * @return The aircraft's type.
     */
    AircraftType getType();

    /**
     * Get the aircraft's designation.
     *
     * @return The aircraft's designation.
     */
    String getDesignation();

    /**
     * Get the aircraft's nation.
     *
     * @return The nation.
     */
    Nation getNationality();

    /**
     * Get the aircraft's service.
     *
     * @return The aircraft's service: the branch of the armed forces.
     */
    ServiceType getService();

    /**
     * Get the aircraft's air to air attack factor.
     *
     * @return The aircraft's air to air attack factor.
     */
    AttackFactor getAir();

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    AttackFactor getLand();

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    AttackFactor getNaval();

    /**
     * Get the aircraft's ferryDistance.
     *
     * @return The aircraft's ferryDistance.
     */
    Range getRange();

    /**
     * Get the aircraft's altitude rating.
     *
     * @return The aircraft's altitude rating.
     */
    AltitudeType getAltitude();

    /**
     * Get the aircraft's landing type. This will be Carrier, land or seaplane.
     *
     * @return The aircraft's landing type.
     */
    LandingType getLanding();

    /**
     * Get the aircraft's frame.
     *
     * @return The aircraft's frame.
     */
    Frame getFrame();
}
