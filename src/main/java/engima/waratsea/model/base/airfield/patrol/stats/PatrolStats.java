package engima.waratsea.model.base.airfield.patrol.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class PatrolStats {
    @Getter
    @Setter
    private Map<Integer, Map<String, PatrolStat>> data;

    @Getter
    @Setter
    private Map<String, String> metaData; // Data describing the patrol stats. The key is the stat's column title.

    /**
     * Constructor.
     */
    public PatrolStats() {
        metaData = new HashMap<>();     // Ensure that the metaData map always exists.
     }
}
