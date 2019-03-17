package engima.waratsea.model.game.event.airfield;

import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Asset;
import engima.waratsea.model.game.Side;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is used to match airfield events. An entity that is looking for a particular airfield event can use this
 * class to detect if the wanted event has occurred.
 */
public class AirfieldEventMatcher {

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    @Setter
    private AirfieldEventAction action;

    @Getter
    @Setter
    private String location;

    @Getter
    @Setter
    private Asset by;

    /**
     * Determines if two ship events are equal.
     *
     * @param firedEvent The airfield event to test for matching.
     * @return True if the airfield event matches. False otherwise.
     */
    public boolean match(final AirfieldEvent firedEvent) {

        return side == firedEvent.getAirfield().getSide()
                && action == firedEvent.getAction()
                && isLocationEqual(firedEvent.getAirfield())
                && isByEqual(firedEvent.getBy());
    }
    /**
     * Determine if the location of the event is matched.
     * @param airfield The airfield that experienced the event.
     * @return True if the ship's location matched. False otherwise.
     */
    private boolean isLocationEqual(final Airfield airfield) {
        return location == null                                                                                         // If the location is not specified then it does not matter.
                || location.equalsIgnoreCase(airfield.getLocation().getReference());
    }

    /**
     * Determine if the asset that caused the event to fire matches.
     * @param eventBy The asset that caused the event to fire.
     * @return True if the asset that caused the event to fire is matched. False otherwise.
     */
    private boolean isByEqual(final Asset eventBy) {
        return by == null                                                                                               // If the by asset is not specified then it does not matter.
                || by.equals(eventBy);
    }
}
