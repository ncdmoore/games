package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.rules.GameRules;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.FunctionalMap;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a reconnaissance aircraft.
 * Supported configurations:
 *
 *  SquadronConfig.NONE
 *  SquadronConfig.REDUCED_PAYLOAD
 *  SquadronConfig.SEARCH
 */
public class Recon implements Aircraft {
    private final Map<AttackType, FunctionalMap<SquadronConfig, Attack>> attackMap = new HashMap<>();

    private final Set<SquadronConfig> configuration = Set.of(
            SquadronConfig.NONE,
            SquadronConfig.REDUCED_PAYLOAD,
            SquadronConfig.SEARCH);

    private static final int ATTACK_REDUCTION = 2; // Squadron configured for search attack factor reduction.

    @Getter private final AircraftId aircraftId;
    @Getter private final AircraftType type;
    @Getter private final String designation;
    @Getter private final Nation nationality;
    @Getter private final ServiceType service;
    @Getter private final AltitudeType altitude;
    @Getter private final LandingType landing;
    @Getter private final LandingType takeoff;
    @Getter private final Frame frame;
    private final Attack navalWarship;
    private final Attack navalTransport;
    private final Attack land;
    private final Attack air;
    private final Performance performance;

    private final Probability probability;
    private final GameRules rules;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param probability Probability utility.
     * @param rules The game rules.
     */
    @Inject
    public Recon(@Assisted final AircraftData data,
                           final Probability probability,
                           final GameRules rules) {
        this.aircraftId = data.getAircraftId();
        this.type = data.getType();
        this.designation = data.getDesignation();
        this.nationality = data.getNationality();
        this.service = data.getService();
        this.altitude = data.getAltitude();
        this.landing = data.getLanding();
        this.takeoff = data.getTakeoff();
        this.navalWarship = new Attack(data.getNavalWarship());
        this.navalTransport = new Attack(data.getNavalTransport());
        this.land = new Attack(data.getLand());
        this.air = new Attack(data.getAir());
        this.performance = new Performance(data.getPerformance());
        this.frame = new Frame(data.getFrame());

        this.probability = probability;
        this.rules = rules;

        probability.setConfigurations(configuration);

        attackMap.put(AttackType.AIR, this::getAir);
        attackMap.put(AttackType.LAND, this::getLand);
        attackMap.put(AttackType.NAVAL_WARSHIP, this::getNavalWarship);
        attackMap.put(AttackType.NAVAL_TRANSPORT, this::getNavalTransport);
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
     * Get the aircraft's squadron configurations.
     *
     * @return The aircraft's allowed squadron configurations.
     */
    @Override
    public Set<SquadronConfig> getConfiguration() {
        return configuration.
                stream()
                .filter(squadronConfig -> rules.isSquadronConfigAllowed(nationality, squadronConfig))
                .collect(Collectors.toSet());
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
        int searchModifier = performance.getSearchModifier(land, navalWarship);
        int reducedModifier = performance.getReducedPayloadModifier(land, navalWarship);

        return Map.of(
                SquadronConfig.NONE, performance.getRadius(),
                SquadronConfig.SEARCH, performance.getRadius() + searchModifier,
                SquadronConfig.REDUCED_PAYLOAD, performance.getRadius() + reducedModifier);
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
        int searchModifier = performance.getSearchModifier(land, navalWarship);
        int reducedModifier = performance.getReducedPayloadModifier(land, navalWarship);

        return Map.of(
                SquadronConfig.NONE, performance.getFerryDistance(),
                SquadronConfig.SEARCH, performance.getFerryDistance() + (searchModifier * 2),
                SquadronConfig.REDUCED_PAYLOAD, performance.getFerryDistance() + (reducedModifier * 2));
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
        return Map.of(
                SquadronConfig.NONE, performance.getEndurance(),
                SquadronConfig.SEARCH, performance.getEndurance(),
                SquadronConfig.REDUCED_PAYLOAD, performance.getEndurance());
    }

    /**
     * Get the aircraft's hit probability.
     *
     * @param attackType The attack type.
     * @param strength The squadron's strength.
     * @return The probability that this aircraft will hit on attack.
     */
    @Override
    public Map<SquadronConfig, Double> getHitProbability(final AttackType attackType, final SquadronStrength strength) {
        return probability.getHitProbability(attackMap.get(attackType).execute(), strength);
    }

    /**
     * Get the probability the aircraft will hit during an attack including any game factors
     * such as weather and type of target.
     *
     *
     * @param attackType The attack type.
     * @param target   The target.
     * @param modifier The circumstance attack modifier: weather, type of target, etc...
     * @return The probability this aircraft will hit in an attack.
     */
    @Override
    public Map<SquadronConfig, Double> getHitIndividualProbability(final AttackType attackType, final Target target, final int modifier) {
        return probability.getIndividualHitProbability(attackMap.get(attackType).execute(), modifier);
    }

    /**
     * Get the aircraft's given attack factor specified by the attack type.
     *
     * @param attackType The type of attack: AIR, LAND or NAVAL.
     * @return Get the aircraft's given attack factor.
     */
    @Override
    public Map<SquadronConfig, Attack> getAttack(final AttackType attackType) {
        return attackMap.get(attackType).execute();
    }

    /**
     * Get the String representation of this class.
     *
     * @return String representation of this class.
     */
    @Override
    public String toString() {
        return aircraftId.getModel();
    }

    /**
     * Compare two aircraft for sorting purposes.
     *
     * @param o the other aircraft.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(final Aircraft o) {
        return getModel().compareTo(o.getModel());
    }

    /**
     * Get the aircraft's air to air attack factor.
     *
     * @return The aircraft's air to air attack factor.
     */
    private Map<SquadronConfig, Attack> getAir() {
        return Map.of(
                SquadronConfig.NONE, air,
                SquadronConfig.SEARCH, air,
                SquadronConfig.REDUCED_PAYLOAD, air);
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    private Map<SquadronConfig, Attack> getLand() {
        Attack reduced = land.getReducedRoundDown(ATTACK_REDUCTION);

        return Map.of(
                SquadronConfig.NONE, land,
                SquadronConfig.SEARCH, reduced,
                SquadronConfig.REDUCED_PAYLOAD, reduced);
    }

    /**
     * Get the aircraft's naval attack factor against warships.
     *
     * @return The aircraft's naval attack factor against warships.
     */
    private Map<SquadronConfig, Attack> getNavalWarship() {
        Attack reduced = navalWarship.getReducedRoundDown(ATTACK_REDUCTION);

        return Map.of(
                SquadronConfig.NONE, navalWarship,
                SquadronConfig.SEARCH, reduced,
                SquadronConfig.REDUCED_PAYLOAD, reduced);
    }

    /**
     * Get the aircraft's naval attack factor against transport.
     *
     * @return The aircraft's naval attack factor against transport.
     */
    private Map<SquadronConfig, Attack> getNavalTransport() {
        Attack reduced = navalTransport.getReducedRoundDown(ATTACK_REDUCTION);

        return Map.of(
                SquadronConfig.NONE, navalTransport,
                SquadronConfig.SEARCH, reduced,
                SquadronConfig.REDUCED_PAYLOAD, reduced);
    }
}
