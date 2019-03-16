package engima.waratsea.model.squadron.allotment.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the allotment for a specific class of aircraft: bomber, fighter or recon.
 */
public class AllotmentTableData {
    @Getter
    @Setter
    private int dice;

    @Getter
    @Setter
    private int factor;

    @Getter
    @Setter
    private List<AllotmentGroupData> groups;
}
