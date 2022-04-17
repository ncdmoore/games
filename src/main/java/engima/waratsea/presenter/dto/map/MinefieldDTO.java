package engima.waratsea.presenter.dto.map;

import engima.waratsea.model.minefield.Minefield;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import lombok.Data;

/**
 * Minefield data transfer object. Used to transfer data from the minefield presenter class to the minefield view classes.
 */
@Data
public class MinefieldDTO {
    private Minefield minefield;
    private MouseEvent event;
    private EventHandler<? super MouseEvent> addMineHandler;
    private EventHandler<? super MouseEvent> removeMineHandler;
}
