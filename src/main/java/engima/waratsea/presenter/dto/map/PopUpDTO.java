package engima.waratsea.presenter.dto.map;

import engima.waratsea.view.map.GridView;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * Map marker interface.
 */
public interface PopUpDTO {

    /**
     * The name of the popup.
     * @return The popup name.
     */
    String getName();

    /**
     * The text contained within the popup.
     * @return The popup text.
     */
    String getTitle();

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

    /**
     * If the popup or its marker is active.
     *
     * @return True if the popup or its marker is active. False otherwise.
     */
    boolean isActive();

}
