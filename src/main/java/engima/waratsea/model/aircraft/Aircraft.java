package engima.waratsea.model.aircraft;

import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;

import java.util.List;

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
     * Get the probability the aircraft will hit in an air-to-air attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    double getAirHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    double getAirHitIndividualProbability(Target target, int modifier);

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a land attack.
     */
    double getLandHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during a land attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance land attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a land attack.
     */
    double getLandHitIndividualProbability(Target target, int modifier);

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a naval attack.
     */
    double getNavalHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during a naval attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance naval attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a naval attack.
     */
    double getNavalHitIndividualProbability(Target target, int modifier);

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
     * Get the aircraft's combat radius. If the aircraft can be
     * equiped with drop tanks then two combar radii are returned:
     * one with drop tanks and one without.
     *
     * @return A list of combar radii.
     */
    List<Integer> getRadius();

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
