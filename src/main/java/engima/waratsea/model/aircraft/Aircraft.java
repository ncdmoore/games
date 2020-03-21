package engima.waratsea.model.aircraft;

import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.target.Target;

import java.util.List;
import java.util.Map;

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
     * Get the mission roles the aircraft is allowed to perform.
     *
     * @return The mission roles the aircraft is allowed to perform.
     */
    List<MissionRole> getRoles();

    /**
     * Get the probability the aircraft will hit in an air-to-air attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    Map<SquadronConfig, Double> getAirHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    Map<SquadronConfig, Double> getAirHitIndividualProbability(Target target, int modifier);

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a land attack.
     */
    Map<SquadronConfig, Double> getLandHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during a land attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance land attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a land attack.
     */
    Map<SquadronConfig, Double> getLandHitIndividualProbability(Target target, int modifier);

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a naval attack.
     */
    Map<SquadronConfig, Double> getNavalHitProbability(SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during a naval attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance naval attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a naval attack.
     */
    Map<SquadronConfig, Double> getNavalHitIndividualProbability(Target target, int modifier);

    /**
     * Get the aircraft's air to air attack factor.
     *
     * @return The aircraft's air to air attack factor.
     */
    Map<SquadronConfig, AttackFactor> getAir();

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    Map<SquadronConfig, AttackFactor> getLand();

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    Map<SquadronConfig, AttackFactor> getNaval();

    /**
     * Get the aircraft's combat radius. This is a map of how the aircraft
     * is configured to the radius of the aircraft under that configuration.
     *
     *  SquadronConfig => combat radius.
     *
     * @return A map of radii based on the aircraft's configuration.
     */
    Map<SquadronConfig, Integer> getRadius();

    /**
     * Get the aircraft's ferry distance. This is a map of how the aircraft
     * is configured to the ferry distance of the aircraft under that configuration.
     *
     *  SquadronConfig => ferry distance.
     *
     * @return A map of ferry distances based on the aircraft's configuration.
     */
    Map<SquadronConfig, Integer> getFerryDistance();

    /**
     * Get the aircraft's range.
     *
     * @return  The aircraft's range.
     */
    int getRange();

    /**
     * Get the aircraft's endurance. This is a map of how the aircraft
     * is configured to the endurance of the aircraft under that configuration.
     *
     *  SquadronConfig => endurance.
     *
     * @return A map of the aircraft's endurance based on the aircraft's configuration.
     */
    Map<SquadronConfig, Integer> getEndurance();

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
