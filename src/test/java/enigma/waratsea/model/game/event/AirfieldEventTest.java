package enigma.waratsea.model.game.event;

import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventAction;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import org.junit.Assert;
import org.junit.Test;

public class AirfieldEventTest {


    @Test
    public void testAirfieldDamagedEvent() {

        Airfield airfield = new Airfield();
        airfield.setName("Rome");

        AirfieldEvent event = new AirfieldEvent();
        event.setAirfield(airfield);
        event.setAction(AirfieldEventAction.DAMAGE);

        AirfieldEventMatcher matcher = new AirfieldEventMatcher();
        matcher.setAction(AirfieldEventAction.DAMAGE);
        matcher.setName("Rome");

        Assert.assertTrue(matcher.match(event));
    }

}
