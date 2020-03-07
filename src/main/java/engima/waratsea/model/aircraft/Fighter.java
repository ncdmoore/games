package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fighter implements Aircraft {
    private static final int BASE_MODIFIER = 1; // A 6 always hits on a 6-sided die.
    private static final double DROP_TANK_FACTOR = 1.5;

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

    private final Dice dice;

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param dice Dice utility.
     */
    @Inject
    public Fighter(@Assisted final AircraftData data,
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
     * Get the probability the aircraft will hit in an air-to-air attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in an air-to-air attack.
     */
    @Override
    public double getAirHitProbability(final SquadronStrength strength) {
        return dice.probability(air.getModifier() + BASE_MODIFIER, air.getFactor(strength));
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
        return dice.individualProbability(air.getModifier() + BASE_MODIFIER + modifier);
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a land attack.
     */
    @Override
    public double getLandHitProbability(final SquadronStrength strength) {
        return dice.probability(land.getModifier() + BASE_MODIFIER, land.getFactor(strength));
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
        return dice.individualProbability(land.getModifier() + BASE_MODIFIER + modifier);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public double getNavalHitProbability(final SquadronStrength strength) {
        return dice.probability(naval.getModifier() + BASE_MODIFIER, naval.getFactor(strength));
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
        return dice.individualProbability(naval.getModifier() + BASE_MODIFIER + modifier);
    }

    /**
     * Get combat radius of the aircraft. There are two radii: one with
     * drop tanks and one without.
     *
     * @return A list of combat radii.
     */
    @Override
    public List<Integer> getRadius() {
        int radius = range.getRadius();
        int radiusWithDropTank = (int) Math.ceil(range.getRadius() * DROP_TANK_FACTOR);
        return new ArrayList<>(Arrays.asList(radius, radiusWithDropTank));
    }
}
