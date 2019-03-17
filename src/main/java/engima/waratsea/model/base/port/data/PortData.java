package engima.waratsea.model.base.port.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents port data that is read in from a JSON file.
 */
public class PortData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String size;

    @Getter
    @Setter
    private int antiAir;

    @Getter
    @Setter
    private String location;
}
