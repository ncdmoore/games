package engima.waratsea.model.squadron;

import engima.waratsea.model.aircraft.Attack;

/**
 * This is a facade class for the aircraft's attack factor based on the
 * squadron's strength.
 */
public class SquadronAttack {
    private final Attack attack;
    private SquadronStrength strength;

    /**
     * Constructor.
     *
     * @param attack An aircraft attack factor.
     */
    public SquadronAttack(final Attack attack) {
        this.attack = attack;
    }

    /**
     * Set the strength.
     *
     * @param squadronStrength The squadron's strength
     * @return This object.
     */
    public SquadronAttack setStrength(final SquadronStrength squadronStrength) {
        strength = squadronStrength;
        return this;
    }

    /**
     * Get the squadron's attack factor.
     *
     * @return The squadron's attack factor at its current strength.
     */
    public int getFactor() {
        return attack.getFactor(strength);
    }

    /**
     * Get squadron's attack modifier.
     *
     * @return The squadron's attack modifier.
     */
    public int getModifier() {
        return attack.getModifier();
    }

    /**
     * Indicates if the attack factor is defensive.
     *
     * @return True if the attack factor is defensive only. False otherwise.
     */
    public boolean isDefensive() {
        return attack.isDefensive();
    }
}
