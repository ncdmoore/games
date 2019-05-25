package engima.waratsea.model.flotilla.data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Flotilla data. For both submarines and MTB (Motor Torpedo Boats)
 */
public class FlotillaData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private List<String> subs;
}
