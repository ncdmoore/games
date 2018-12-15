package engima.waratsea.view.map;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Marker objects.
 */
public class MarkerDTO {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mapRef;

    @Getter
    @Setter
    private int x;

    @Getter
    @Setter
    private int y;

    @Getter
    @Setter
    private int size;

    @Getter
    @Setter
    private boolean active;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> eventHandler;
}
