package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.AttackData;
import engima.waratsea.model.squadron.SquadronStrength;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aircraft's attack for air, land or naval.
 */
public class Attack {
    @Getter private final int modifier;      // Determines which values of a six sided die are hits. For example a modifier of
                                             // 1 indicates that both a 5 and a 6 are hits. Note, a 6 is always a hit;
                                             // i.e. a modifier of 0 indicates that only a 6 is a hit.
    @Getter private final int full;          // Total number of dice rolled for a full strength squadron.
    @Getter private final int half;          // Total number of dice rolled for a half strength squadron.
    @Getter private final int sixth;         // Total number of dice rolled for one-sixth strength squadron.
    @Getter private final boolean defensive; // Indicates if the factor is defensive only. Only returns fire does not initiate.

    private final Map<SquadronStrength, Integer> factor = new HashMap<>();

    @Getter private final double finalModifier;  // The final factor in determining a successful attack.

    /**
     * Constructor.
     *
     * @param data The attack factor data read in from a JSON file.
     */
    public Attack(final AttackData data) {
        this.modifier = data.getModifier();
        this.full = data.getFull();
        this.half = data.getHalf();
        this.sixth = data.getSixth();
        this.defensive = data.isDefensive();

        factor.put(SquadronStrength.FULL, full);
        factor.put(SquadronStrength.HALF, half);
        factor.put(SquadronStrength.SIXTH, sixth);

        this.finalModifier = data.getFinalModifier();
    }

    /**
     * Get the attack factor.
     *
     * @param strength The strength of the squadron.
     * @return The attach factor based on the squadron's strength.
     */
    public int getFactor(final SquadronStrength strength) {
        return factor.get(strength);
    }

    /**
     * Get the reduced attack factor.
     *
     * @param reduction The reduction factor.
     * @return A reduced attack factor.
     */
    public Attack getReducedRoundDown(final int reduction) {
        AttackData data = AttackData
                .builder()
                .modifier(modifier)
                .full(full / reduction)
                .half(half / reduction)
                .sixth(sixth / reduction)
                .finalModifier(finalModifier)
                .build();

        return new Attack(data);
    }

    /**
     * Get the reduced attack factor.
     *
     * @param reduction The reduction factor.
     * @return A reduced attack factor.
     */
    public Attack getReducedRoundUp(final int reduction) {
        AttackData data = AttackData
                .builder()
                .modifier(modifier)
                .full((full / reduction) + (full % reduction))
                .half((half / reduction) + (half % reduction))
                .sixth((sixth / reduction) + (sixth % reduction))
                .build();

        return new Attack(data);
    }
}

