package engima.waratsea.model.squadron.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the squadron allotment data for a nation. The allotment of bombers, fighters and recon squadrons.
 */
public class AllotmentData {
    @Getter
    @Setter
    private AllotmentAircraftClassData bombers;

    @Getter
    @Setter
    private AllotmentAircraftClassData fighters;

    @Getter
    @Setter
    private AllotmentAircraftClassData recon;
}
