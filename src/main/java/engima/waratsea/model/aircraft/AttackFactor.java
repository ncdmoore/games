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
    private final int modifier;

    @Getter
    private final int full;

    @Getter
    private final int half;

    @Getter
    private final int sixth;

    @Getter
    private final boolean defensive;

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
}
