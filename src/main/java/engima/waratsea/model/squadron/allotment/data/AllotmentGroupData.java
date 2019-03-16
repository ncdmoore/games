package engima.waratsea.model.squadron.allotment.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents an allotment group.
 */
public class AllotmentGroupData {
    @Getter
    @Setter
    private int selectSize;

    @Getter
    @Setter
    private List<AllotmentAircraftData> aircraft;
}
