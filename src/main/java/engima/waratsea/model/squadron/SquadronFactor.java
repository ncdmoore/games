package engima.waratsea.model.squadron;

import engima.waratsea.model.aircraft.AttackFactor;

/**
 * This is a facade class for the aircraft's attack factor based on the
 * squadron's strength.
 */
public class SquadronFactor {
    private final AttackFactor attackFactor;
    private SquadronStrength strength;

    /**
     * Constructor.
     *
     * @param factor An aircraft attack factor.
     */
    public SquadronFactor(final AttackFactor factor) {
        this.attackFactor = factor;
    }

    /**
     * Set the strength.
     *
     * @param squadronStrength The squadron's strength
     * @return This object.
     */
    public SquadronFactor setStrength(final SquadronStrength squadronStrength) {
        strength = squadronStrength;
        return this;
    }

    /**
     * Get the squadron's attack factor.
     *
     * @return The squadron's attack factor at its current strength.
     */
    public int getFactor() {
        return attackFactor.getFactor(strength);
    }

    /**
     * Get squadron's attack modifier.
     *
     * @return The squadron's attack modifier.
     */
    public int getModifier() {
        return attackFactor.getModifier();
    }

    /**
     * Indicates if the attack factor is defensive.
     *
     * @return True if the attack factor is defensive only. False otherwise.
     */
    public boolean isDefensive() {
        return attackFactor.isDefensive();
    }
}
