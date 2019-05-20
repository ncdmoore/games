package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.minefield.Minefield;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * Minefield data transfer object. Used to transfer data from the minefield presenter class to the minefield view classes.
 */
public class MinefieldDTO {

    @Getter
    @Setter
    private Minefield minefield;

    @Getter
    @Setter
    private MouseEvent event;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> addMineHandler;

    @Getter
    @Setter
    private EventHandler<? super MouseEvent> removeMineHandler;
}
