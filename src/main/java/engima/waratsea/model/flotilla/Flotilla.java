package engima.waratsea.model.flotilla;

import engima.waratsea.model.submarine.Submarine;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a flotilla.
 */
public class Flotilla {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private List<Submarine> subs;
}
