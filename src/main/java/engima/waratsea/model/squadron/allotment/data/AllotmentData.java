package engima.waratsea.model.squadron.allotment.data;

import engima.waratsea.model.game.Nation;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the squadron allotment data for a nation. The allotment of bombers, fighters and recon squadrons.
 */
public class AllotmentData {
    @Getter
    @Setter
    private Nation nation;

    @Getter
    @Setter
    private int maxOptionalDice;

    @Getter
    @Setter
    private AllotmentTableData bombers;

    @Getter
    @Setter
    private AllotmentTableData fighters;

    @Getter
    @Setter
    private AllotmentTableData recon;
}
