package engima.waratsea.model.flotilla.data;

import lombok.Data;

import java.util.List;

/**
 * Flotilla data. For both submarines and MTB (Motor Torpedo Boats)
 */
@Data
public class FlotillaData {
    private String name;
    private String location;
    private List<String> subs;
    private List<String> boats;
}
