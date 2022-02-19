package engima.waratsea.model.base.airfield.mission.stats;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class ProbabilityStats {
    private String title;
    private String eventColumnTitle;

    @Builder.Default
    private String probabilityColumnTitle = "Probability";

    @Builder.Default
    private Map<String, Integer> metaData = new HashMap<>();         // Data describing the stats. The key is the event column title.

    private Map<String, Integer> probability;
}
