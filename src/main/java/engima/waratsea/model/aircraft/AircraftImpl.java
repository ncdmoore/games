package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.base.airfield.mission.MissionRole;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Represents an aircraft.
 */
public class AircraftImpl implements Aircraft {

    private static final int BASE_FACTOR = 1; // A  6 on a 6-sided die always hits.

    @Getter
    private final AircraftId aircraftId;

    @Getter
    private final AircraftType type;

    @Getter
    private final String designation;

    @Getter
    private final Nation nationality;

    @Getter
    private final ServiceType service;

    @Getter
    private final AltitudeType altitude;

    @Getter
    private final LandingType landing;

    @Getter
    private final AttackFactor naval;

    @Getter
    private final AttackFactor land;

    @Getter
    private final AttackFactor air;

    @Getter
    private final Range range;

    @Getter
    private final Frame frame;

    @Getter
    private final Dice dice;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param dice Dice utility.
     */
    @Inject
    public AircraftImpl(@Assisted final AircraftData data,
                                  final Dice dice) {
        this.aircraftId = data.getAircraftId();
        this.type = data.getType();
        this.designation = data.getDesignation();
        this.nationality = data.getNationality();
        this.service = data.getService();
        this.altitude = data.getAltitude();
        this.landing = data.getLanding();
        this.naval = new AttackFactor(data.getNaval());
        this.land = new AttackFactor(data.getLand());
        this.air = new AttackFactor(data.getAir());
        this.range = new Range(data.getRange());
        this.frame = new Frame(data.getFrame());

        this.dice = dice;
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
     * The aircraft's combat radius.
     *
     * @return The aircraft's combat radius inside a list.
     */
    @Override
    public List<Integer> getRadius() {
        return Collections.singletonList(range.getRadius());
    }

    /**
     * Get the aircraft's ferry distance. If the aircraft can be
     * equipped with drop tanks then two ferry distances are returned:
     * one with drop tanks and one without.
     *
     * @return A list of ferry distances.
     */
    @Override
    public List<Integer> getFerryDistance() {
        return Collections.singletonList(range.getFerryDistance());
    }

    /**
     * Get the aircraft's air-to-air hit probability.
     *
     * @param strength The squadron's strength.
     * @return The probability that this aircraft will hit on an air-to-air attack.
     */
    @Override
    public double getAirHitProbability(final SquadronStrength strength) {
        return dice.probability(air.getModifier() + BASE_FACTOR, air.getFactor(strength));
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
    public double getAirHitIndividualProbability(final Target target, final int modifier) {
        return dice.individualProbability(air.getModifier() + BASE_FACTOR + modifier);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a land attack.
     */
    @Override
    public double getLandHitProbability(final SquadronStrength strength) {
        return dice.probability(land.getModifier() + BASE_FACTOR, land.getFactor(strength));
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
    public double getLandHitIndividualProbability(final Target target, final int modifier) {
        return dice.individualProbability(land.getModifier() + BASE_FACTOR + modifier);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return The probability this aircraft will hit in a naval attack.
     */
    @Override
    public double getNavalHitProbability(final SquadronStrength strength) {
        return dice.probability(naval.getModifier() + BASE_FACTOR, naval.getFactor(strength));
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
    public double getNavalHitIndividualProbability(final Target target, final int modifier) {
        return dice.individualProbability(naval.getModifier() + BASE_FACTOR + modifier);
    }
}
