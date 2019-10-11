package engima.waratsea.model.aircraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import engima.waratsea.model.aircraft.data.AircraftData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.utility.Dice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fighter implements Aircraft {
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
    public int getAirHitProbability(final SquadronStrength strength) {
        return dice.probability6(air.getModifier() + 1, air.getFactor(strength));
    }

    /**
     * Get the probability the aircraft will hit in a land attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a land attack.
     */
    @Override
    public int getLandHitProbability(final SquadronStrength strength) {
        return dice.probability6(land.getModifier() + 1, land.getFactor(strength));
    }

    /**
     * Get the probability the aircraft will hit during a naval attack.
     *
     * @param strength The strength of the squadron.
     * @return A percentage representing the probability this aircraft will hit in a naval attack.
     */
    @Override
    public int getNavalHitProbability(final SquadronStrength strength) {
        return dice.probability6(naval.getModifier() + 1, naval.getFactor(strength));
    }

    /**
     * Get combat radius of the aircraft. There are two radii: one with
     * drop tanks and one without.
     *
     * @return A list of combat radii.
     */
    public List<Integer> getRadius() {
        int radius = range.getRadius();
        int radiusWithDropTank = (int) Math.ceil(range.getRadius() * DROP_TANK_FACTOR);
        return new ArrayList<>(Arrays.asList(radius, radiusWithDropTank));
    }
}
