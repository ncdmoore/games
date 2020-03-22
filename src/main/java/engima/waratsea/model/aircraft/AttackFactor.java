package engima.waratsea.model.aircraft;

import engima.waratsea.model.aircraft.data.AttackFactorData;
import engima.waratsea.model.squadron.SquadronStrength;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an aircraft's attack factor for air, land or naval.
 */
public class AttackFactor {
    @Getter
    private final int modifier;              // Determines which values of a six sided die are hits. For example a modifier of
                                             // 1 indicates that both a 5 and a 6 are hits.
    @Getter private final int full;          // Total number of dice rolled for a full strength squadron.
    @Getter private final int half;          // Total number of dice rolled for a half strength squadron.
    @Getter private final int sixth;         // Total number of dice rolled for one-sixth strength squadron.
    @Getter private final boolean defensive; // Indicates if the factor is defensive only. Only returns fire does not initiate.

    private final Map<SquadronStrength, Integer> factor = new HashMap<>();

    /**
     * Constructor.
     *
     * @param data The attack factor data read in from a JSON file.
     */
    public AttackFactor(final AttackFactorData data) {
        this.modifier = data.getModifier();
        this.full = data.getFull();
        this.half = data.getHalf();
        this.sixth = data.getSixth();
        this.defensive = data.isDefensive();

        factor.put(SquadronStrength.FULL, full);
        factor.put(SquadronStrength.HALF, half);
        factor.put(SquadronStrength.SIXTH, sixth);
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
    public AttackFactor getReducedRoundDown(final int reduction) {
        AttackFactorData data = new AttackFactorData();
        data.setModifier(modifier);
        data.setFull(full / reduction);
        data.setHalf(half / reduction);
        data.setSixth(sixth / reduction);
        return new AttackFactor(data);
    }

    /**
     * Get the reduced attack factor.
     *
     * @param reduction The reduction factor.
     * @return A reduced attack factor.
     */
    public AttackFactor getReducedRoundUp(final int reduction) {
        AttackFactorData data = new AttackFactorData();
        data.setModifier(modifier);
        data.setFull((full / reduction) + (full % reduction));
        data.setHalf((half / reduction) + (half % reduction));
        data.setSixth((sixth / reduction) + (sixth % reduction));
        return new AttackFactor(data);
    }
}

