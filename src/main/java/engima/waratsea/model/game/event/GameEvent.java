package engima.waratsea.model.game.event;

import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.scenario.ScenarioEvent;
import engima.waratsea.model.game.event.ship.ShipEvent;
import engima.waratsea.model.game.event.squadron.SquadronEvent;
import engima.waratsea.model.game.event.turn.TurnEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * This is the event base class.
 */
@Slf4j
public abstract class GameEvent {

    /**
     * Initialize all game events.
     */
    public static void init() {
        ShipEvent.init();
        SquadronEvent.init();
        AirfieldEvent.init();
        TurnEvent.init();
        ScenarioEvent.init();
    }

    /**
     * Fire the game event.
     */
    public abstract void fire();
}
