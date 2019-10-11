package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.utility.Dice;

/**
 * Represents an Italian level bomber.
 */
public class PoorNavalBomber extends AircraftImpl {

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
    public int getNavalHitProbability(final SquadronStrength strength) {

        final double poorNavalFactor = 2.0 / 6.0;

        return (int) (getDice().probability6(getNaval().getModifier() + 1, getNaval().getFactor(strength)) * poorNavalFactor);
    }
}
