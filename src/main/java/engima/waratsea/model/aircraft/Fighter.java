package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.aircraft.data.AttackFactorData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.target.Target;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a fighter aircraft.
 *
 * Fighters are unique in that they alone may perform CAP Patrols and Sweep Missions.
 * Fighters may also be equipped with drop tanks to further extend their range. However,
 * this does reduce their air-to-air effectiveness.
 *
 * Supported configurations:
 *
 *  SquadronConfig.NONE
 *  SquadronConfig.DROP_TANKS
 *  SquadronConfig.SEARCH
 *  SquadronConfig.STRIPPED_DOWN
 */
public class Fighter implements Aircraft {
    private static final Set<SquadronConfig> CONFIGS = Set.of(
            SquadronConfig.NONE,
            SquadronConfig.DROP_TANKS,
            SquadronConfig.SEARCH,
            SquadronConfig.STRIPPED_DOWN);

    private static final double DROP_TANK_FACTOR = 1.5;    // Drop tanks increase range by 1.5 times.
    private static final int STRIPPED_DOWN_FACTOR = 3;     // Stripping down the fighter of ordinance and adding extra full increases the range by 3.

    @Getter private final AircraftId aircraftId;
    @Getter private final AircraftType type;
    @Getter private final String designation;
    @Getter private final Nation nationality;
    @Getter private final ServiceType service;
    @Getter private final AltitudeType altitude;
    @Getter private final LandingType landing;
    @Getter private final LandingType takeoff;
    private final AttackFactor naval;
    private final AttackFactor land;
    private final AttackFactor air;
    private final Performance performance;
    @Getter private final Frame frame;
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
        return Arrays.asList(MissionRole.MAIN, MissionRole.ESCORT);
    }

    /**
     * Get the probability the aircraft will hit in an air-to-air attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in an air-to-air attack.
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
     * @return A percentage representing the probability this aircraft will hit in a land attack.
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
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
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
        AttackFactorData data = new AttackFactorData();
        data.setFull(air.getFull());
        data.setHalf(air.getHalf());
        data.setSixth(air.getSixth());
        AttackFactor stripped = new AttackFactor(data);

        return Map.of(SquadronConfig.NONE, air,
                SquadronConfig.DROP_TANKS, air,
                SquadronConfig.SEARCH, air,
                SquadronConfig.STRIPPED_DOWN, stripped);
    }

    /**
     * Get the aircraft's land attack factor.
     *
     * @return The aircraft's land attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getLand() {
        AttackFactorData data = new AttackFactorData();   // a zero attack factor.
        AttackFactor stripped = new AttackFactor(data);

        return Map.of(SquadronConfig.NONE, land,
                      SquadronConfig.DROP_TANKS, land,
                      SquadronConfig.SEARCH, land,
                      SquadronConfig.STRIPPED_DOWN, stripped);
    }

    /**
     * Get the aircraft's naval attack factor.
     *
     * @return The aircraft's naval attack factor.
     */
    @Override
    public Map<SquadronConfig, AttackFactor> getNaval() {
        AttackFactorData data = new AttackFactorData();   // a zero attack factor.
        AttackFactor stripped = new AttackFactor(data);

        return Map.of(SquadronConfig.NONE, naval,
                      SquadronConfig.DROP_TANKS, naval,
                      SquadronConfig.SEARCH, naval,
                      SquadronConfig.STRIPPED_DOWN, stripped);
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
        int radius = performance.getRadius();
        int radiusWithDropTank = (int) Math.ceil(performance.getRadius() * DROP_TANK_FACTOR);
        int radiusStrippedDown = performance.getRadius() * STRIPPED_DOWN_FACTOR;

        return Map.of(SquadronConfig.NONE, radius,
                      SquadronConfig.DROP_TANKS, radiusWithDropTank,
                      SquadronConfig.SEARCH, radiusWithDropTank,
                      SquadronConfig.STRIPPED_DOWN, radiusStrippedDown);
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
        int distance = performance.getFerryDistance();
        int distanceWithDropTank = (int) Math.ceil(performance.getFerryDistance() * DROP_TANK_FACTOR);
        int distanceStrippedDown = performance.getFerryDistance() * STRIPPED_DOWN_FACTOR;

        return Map.of(SquadronConfig.NONE, distance,
                      SquadronConfig.DROP_TANKS, distanceWithDropTank,
                      SquadronConfig.SEARCH, distanceWithDropTank,
                      SquadronConfig.STRIPPED_DOWN, distanceStrippedDown);
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
                      SquadronConfig.DROP_TANKS, performance.getEndurance(),
                      SquadronConfig.SEARCH, performance.getEndurance(),
                      SquadronConfig.STRIPPED_DOWN, performance.getEndurance());
    }
}
