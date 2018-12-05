package engima.waratsea.event;

import lombok.Getter;
import lombok.Setter;

/**
 * Indicates that a date event has occurred in the game. Date events occur when the turn progresses to where the
 * day changes.
 */
public class DateEvent extends GameEvent {

    @Getter
    @Setter
    private String date;
}
