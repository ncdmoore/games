package engima.waratsea.model.squadron.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Represents squadron data that is read and written to a JSON file.
 */
public class SquadronData {
    @Getter @Setter private transient Side side;
    @Getter @Setter private String name;
    @Getter @Setter private Nation nation;
    @Getter @Setter private String model;
    @Getter @Setter private SquadronStrength strength;
    @Getter @Setter private String airfield;
    @Setter private SquadronState squadronState;
    @Getter @Setter private SquadronConfig config;
    @Getter @Setter private int missionId;

    /**
     * Get the state of the squadron.
     *
     * @return The squadron's state.
     */
    public SquadronState getSquadronState() {
        return Optional
                .ofNullable(squadronState)
                .orElse(SquadronState.READY);
    }
}
