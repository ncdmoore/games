package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.target.Target;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a bomber aircraft.
 *
 * Squadron configurations supported:
 *
 *  SquadronConfig.NONE
 *  SquadronConfig.SEARCH
 */
public class Bomber implements Aircraft {
    private static final Set<SquadronConfig> CONFIGS = Set.of(SquadronConfig.NONE, SquadronConfig.SEARCH);

    private static final int SEARCH_MODIFIER = 4;         // Squadron configured for search has less ordinance and more fuel. This is the increase in range.
    private static final int SEARCH_ATTACK_REDUCTION = 2; // Squadron configured for search attack factor reduction.

    @Getter private final AircraftId aircraftId;
    @Getter private final AircraftType type;
    @Getter private final String designation;
    @Getter private final Nation nationality;
    @Getter private final ServiceType service;
    @Getter private final AltitudeType altitude;
    @Getter private final LandingType landing;
    @Getter private final LandingType takeoff;
    private final AttackFactor naval;
    private final AttackFactor air;
    private final AttackFactor land;
    @Getter private final Frame frame;
    @Getter(AccessLevel.PROTECTED) private final Probability probability;
    @Getter(AccessLevel.PROTECTED) private final Performance performance;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param probability Probability utility.
     */
    @Inject
    public Bomber(@Assisted final AircraftData data,
                            final Probability probability) {
        this.aircraftId = data.getAircraftId();
        this.type = data.getType();
        this.designation = data.getDesignation();
        this.nationality = data.getNationality();
        this.service = data.getService();
        this.altitude = data.getAltitude();
        this.landing = data.getLanding();
        this.takeoff = data.getTakeoff();
        this.naval = new AttackFactor(data.getNaval());
        this.land = new AttackFactor(data.getLand());
        this.air = new AttackFactor(data.getAir());
        this.performance = new Performance(data.getPerformance());
        this.frame = new Frame(data.getFrame());

        this.probability = probability;

        probability.setConfigurations(CONFIGS);
    }

    /**
     * Get the aircraft's model.
     *
     * @return The aircraft's model.
     */
    @Override
    public String getModel() {
        return aircraftId.getModel();
    }

    /**
     * Get the aircraft's side.
     *
     * @return The aircraft's side.
     */
    @Override
    public Side getSide() {
        return aircraftId.getSide();
    }

    /**
     * Get the mission roles the aircraft is allowed to perform.
     *
     * @return The mission roles the aircraft is allowed to perform.
     */
    @Override
    public List<MissionRole> getRoles() {
        return Collections.singletonList(MissionRole.MAIN);
    }

    /**
     * Get the aircraft's combat radius. This is a map of how the aircraft
     * is configured to the radius of the aircraft under that configuration.
     * <p>
     * SquadronConfig => combat radius.
     *
     * @return A map of radii based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getRadius() {
       return Map.of(SquadronConfig.NONE, performance.getRadius(),
                     SquadronConfig.SEARCH, performance.getRadius() + SEARCH_MODIFIER);
    }

    /**
     * Get the aircraft's ferry distance. This is a map of how the aircraft
     * is configured to the ferry distance of the aircraft under that configuration.
     *
     *  SquadronConfig => ferry distance.
     *
     * @return A map of ferry distances based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getFerryDistance() {
        return Map.of(SquadronConfig.NONE, performance.getFerryDistance(),
                      SquadronConfig.SEARCH, performance.getFerryDistance() + (SEARCH_MODIFIER * 2));
    }

    /**
     * Get the aircraft's range.
     *
     * @return The aircraft's range.
     */
    @Override
    public int getRange() {
        return performance.getGameRange();
    }

    /**
     * Get the aircraft's endurance. This is a map of how the aircraft
     * is configured to the endurance of the aircraft under that configuration.
     *
     *  SquadronConfig => endurance.
     *
     * @return A map of the aircraft's endurance based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getEndurance() {
        return Map.of(SquadronConfig.NONE, performance.getEndurance(),
                      SquadronConfig.SEARCH, performance.getEndurance());
    }

    /**
     * Get the aircraft's air-to-air hit probability.
     *
     * @param strength The squadron's strength.
     * @return The probability that this aircraft will hit on an air-to-air attack.
     */
    @Override
    public  Map<SquadronConfig, Double> getAirHitProbability(final SquadronStrength strength) {
       return probability.getHitProbability(getAir(), strength);
    }

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     * @param target   The target.
     * @param modifier The circumstance air-to-air attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an air-to-air attack.
     */
    @Override
    public Map<SquadronConfig, Double> getAirHitIndividualProbability(final Target target, final int modifier) {
       return probability.getIndividualHitProbability(getAir(), modifier);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitProbability(final SquadronStrength strength) {
        return probability.getHitProbability(getLand(), strength);
    }

    /**
     * Get the probability the aircraft will hit during a land attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance land attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a land attack.
     */
    @Override
    public Map<SquadronConfig, Double> getLandHitIndividualProbability(final Target target, final int modifier) {
        return probability.getIndividualHitProbability(getLand(), modifier);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getNavalHitProbability(final SquadronStrength strength) {
        return probability.getHitProbability(getNaval(), strength);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack including in game factors
     * such as weather and type of target.
     *
     * @param target The target.
     * @param modifier The circumstance naval attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in a naval attack.
     */
    @Override
    public Map<SquadronConfig, Double> getNavalHitIndividualProbability(final Target target, final int modifier) {
        return probability.getIndividualHitProbability(getNaval(), modifier);
    }

    /**
     * Get the aircraft's air to air attack factor.
     *
     * @return The aircraft's air to air attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getAir() {
        return Map.of(SquadronConfig.NONE, air,
                      SquadronConfig.SEARCH, air);
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getLand() {
        AttackFactor search = land.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, land,
                      SquadronConfig.SEARCH, search);
    }

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getNaval() {
        AttackFactor search = naval.getReducedRoundDown(SEARCH_ATTACK_REDUCTION);

        return Map.of(SquadronConfig.NONE, naval,
                      SquadronConfig.SEARCH, search);
    }
}
