package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.target.Target;
import engima.waratsea.model.target.TargetEnemyPort;
import engima.waratsea.model.target.TargetEnemyTaskForce;
import engima.waratsea.utility.Dice;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Italian level bomber.
 */
public class PoorNavalBomber extends AircraftImpl {

    private static final int BASE_FACTOR = 1; // 6 always hits on a 6-sided die.
    private static final double POOR_NAVAL_FACTOR = 2.0 / 6.0;

    private static final Map<Class<?>, Double> FACTOR_MAP = new HashMap<>();

    static {
        FACTOR_MAP.put(TargetEnemyPort.class, 1.0);                       // No penalty when attacking ships in ports.
        FACTOR_MAP.put(TargetEnemyTaskForce.class, POOR_NAVAL_FACTOR);    // Penalty is applied when attacking ships at sea.
    }

    /**
     * The constructor called by guice.
     *
     * @param data The aircraft data read in from a JSON file.
     * @param dice Dice utility.
     */
    @Inject
    public PoorNavalBomber(@Assisted final AircraftData data,
                                     final Dice dice) {
        super(data, dice);
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public double getNavalHitProbability(final SquadronStrength strength) {
        return getDice().probability(getNaval().getModifier() + BASE_FACTOR, getNaval().getFactor(strength)) * POOR_NAVAL_FACTOR;
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
        double factor = FACTOR_MAP.getOrDefault(target.getClass(), POOR_NAVAL_FACTOR);
        return getDice().individualProbability(getNaval().getModifier() + BASE_FACTOR + modifier) * factor;
    }
}
