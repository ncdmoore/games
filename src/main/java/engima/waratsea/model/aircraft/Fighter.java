package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.FunctionalMap;
import javafx.util.Pair;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a fighter aircraft.
 *
 * Fighters are unique in that they alone may perform CAP Patrols and Sweep Missions.
 * Fighters may also be equipped with drop tanks to further extend their range. However,
 * this does reduce their air-to-air effectiveness.
 */
public class Fighter implements Aircraft {
    private final Map<AttackType, FunctionalMap<SquadronConfig, Attack>> attackMap = new HashMap<>();

    @Getter private final AircraftId aircraftId;
    @Getter private final String name;
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
    @Getter private final Set<SquadronConfig> configuration;

    private final Probability probability;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param probability Probability utility.
     */
    @Inject
    public Fighter(@Assisted final AircraftData data,
                             final Probability probability) {
        this.aircraftId = data.getAircraftId();
        this.name = data.getName();
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
        this.configuration = Optional
                .ofNullable(data.getConfig())
                .orElse(Set.of(SquadronConfig.NONE));

        this.probability = probability;

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
        return Arrays.asList(MissionRole.MAIN, MissionRole.ESCORT);
    }

    /**
     * Get the probability the aircraft will hit in an attack.
     *
     * @param attackType The attack type.
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in an attack.
     */
    @Override
    public  Map<SquadronConfig, Double> getHitProbability(final AttackType attackType, final SquadronStrength strength) {
        return probability.getHitProbability(attackMap.get(attackType).execute(), strength);
    }

    /**
     * Get the probability the aircraft will hit during air-to-air attack including any game factors
     * such as weather and type of target.
     *
     *
     * @param attackType The attack type.
     * @param target   The target.
     * @param modifier The circumstance an attack modifier: weather, type of target, etc...
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
     * Get the aircraft's combat radius. This is a map of how the aircraft
     * is configured to the radius of the aircraft under that configuration.
     *
     *  SquadronConfig => combat radius.
     *
     * @return A map of radii based on the aircraft's configuration.
     */
    @Override
    public Map<SquadronConfig, Integer> getRadius() {
        return configuration
                .stream()
                .map(this::buildRadius)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
        return configuration
                .stream()
                .map(this::buildFerryDistance)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
        return configuration
                .stream()
                .map(config -> new Pair<>(config, config.getEndurance(performance)))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
        return configuration
                .stream()
                .map(this::buildAir)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    private Map<SquadronConfig, Attack> getLand() {
        return configuration
                .stream()
                .map(this::buildLand)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Get the aircraft's naval attack factor against warships.
     *
     * @return The aircraft's naval attack factor against worships.
     */
    private Map<SquadronConfig, Attack> getNavalWarship() {
        return configuration
                .stream()
                .map(this::buildNavalWarship)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Get the aircraft's naval attack factor against transports.
     *
     * @return The aircraft's naval attack factor against transports.
     */
    private Map<SquadronConfig, Attack> getNavalTransport() {
        return configuration
                .stream()
                .map(this::buildNavalTransport)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Pair<SquadronConfig, Integer> buildRadius(final SquadronConfig config) {
        return new Pair<>(config, config.getRadius(land, navalWarship, performance));
    }

    private Pair<SquadronConfig, Integer> buildFerryDistance(final SquadronConfig config) {
        return new Pair<>(config, config.getFerryDistance(land, navalWarship, performance));
    }

    private Pair<SquadronConfig, Attack> buildAir(final SquadronConfig config) {
        return new Pair<>(config, config.getAttack(AttackType.AIR, air));
    }

    private Pair<SquadronConfig, Attack> buildLand(final SquadronConfig config) {
        return new Pair<>(config, config.getAttack(AttackType.LAND, land));
    }

    private Pair<SquadronConfig, Attack> buildNavalWarship(final SquadronConfig config) {
        return new Pair<>(config, config.getAttack(AttackType.NAVAL_WARSHIP, navalWarship));
    }

    private Pair<SquadronConfig, Attack> buildNavalTransport(final SquadronConfig config) {
        return new Pair<>(config, config.getAttack(AttackType.NAVAL_TRANSPORT, navalTransport));
    }
}
