package engima.waratsea.model.squadron.data;

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
    private List<String> aircraft;
}
