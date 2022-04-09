package engima.waratsea.model.squadron.data;

import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.SquadronConfig;
import engima.waratsea.model.squadron.SquadronStrength;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Data;

import java.util.Optional;

/**
 * Represents squadron data that is read and written to a JSON file.
 */
@Data
public class SquadronData {
    private transient Side side;
    private String name;
    private Nation nation;
    private String model;
    private SquadronStrength strength;
    private String airfield;
    private SquadronState squadronState;
    private SquadronConfig config;
    private int missionId;

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
