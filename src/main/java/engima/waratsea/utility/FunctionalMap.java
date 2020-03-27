package engima.waratsea.utility;

import java.util.Map;

public interface FunctionalMap<R1, R2> {
    /**
     * Execute the function.
     * @return A map of results.
     */
    Map<R1, R2> execute();

}
