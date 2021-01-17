package engima.waratsea.model.base.airfield.mission.stats;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class ProbabilityStats {
    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String eventColumnTitle;

    @Getter
    @Setter
    private String probabilityColumnTitle = "Probability";

    @Getter
    @Setter
    private Map<String, Integer> metaData; // Data describing the stats. The key is the event column title.

    @Getter
    @Setter
    private Map<String, Integer> probability;

    public ProbabilityStats() {
        metaData = new HashMap<>(); // Ensure the meta data map always exists.
    }
}
