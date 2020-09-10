package engima.waratsea.model.aircraft;

import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents an aircraft.
 */
public interface Aircraft extends Comparable<Aircraft> {

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
     * Get the aircraft's squadron configurations.
     *
     * @return The aircraft's allowed squadron configurations.
     */
    Set<SquadronConfig> getConfiguration();

    /**
     * Get the mission roles the aircraft is allowed to perform.
     *
     * @return The mission roles the aircraft is allowed to perform.
     */
    List<MissionRole> getRoles();

    /**
     * Get the probability the aircraft will hit in an  attack.
     *
     * @param attackType The attack type: AIR, LAND or NAVAL.
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in an attack.
     */
    Map<SquadronConfig, Double> getHitProbability(AttackType attackType, SquadronStrength strength);

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     *
     * @param attackType The attack type.
     * @param target The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    Map<SquadronConfig, Double> getHitIndividualProbability(AttackType attackType, Target target, int modifier);

    /**
     * Get the aircraft's given attack factor specified by the attack type.
     *
     * @param attackType The type of attack: AIR, LAND or NAVAL.
     * @return Get the aircraft's given attack factor.
     */
    Map<SquadronConfig, AttackFactor> getAttack(AttackType attackType);

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
