package engima.waratsea.presenter.dto.map;

import engima.waratsea.view.map.GridView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Map marker interface.
 */
public interface PopUpDTO {

    /**
     * The text contained within the popup.
     * @return The popup text.
     */
    String getText();

    /**
     * The map reference that the popup annotates.
     * @return The corresponding popup map reference.
     */
    String getMapReference();

    /**
     * The map grid view that the popup annotates.
     * @return The corresponding popup grid view.
     */
    GridView getGridView();


    /**
     * How far in pixels the popup's top corner is x-offset from the grid.
     * @return The popup's x-coordinate offset.
     */
    int getXOffset();

    /**
     * The CSS style for the popup.
     * @return The CSS style.
     */
    String getStyle();

    /**
     * The popup event handler. This is the handler that is called when the popup is clicked.
     * @return The popup event handler.
     */
    EventHandler<? super MouseEvent> getPopupEventHandler();


}
