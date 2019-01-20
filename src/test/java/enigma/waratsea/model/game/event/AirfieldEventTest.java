package enigma.waratsea.model.game.event;

import engima.waratsea.model.airfield.Airfield;
import engima.waratsea.model.airfield.data.AirfieldData;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.game.event.airfield.AirfieldEvent;
import engima.waratsea.model.game.event.airfield.AirfieldEventAction;
import engima.waratsea.model.game.event.airfield.AirfieldEventMatcher;
import org.junit.Assert;
import org.junit.Test;

public class AirfieldEventTest {


    @Test
    public void testAirfieldDamagedEvent() {

        AirfieldData data = new AirfieldData();
        data.setName("Rome");

        Airfield airfield = new Airfield(Side.ALLIES, data);

        AirfieldEvent event = new AirfieldEvent();
        event.setAirfield(airfield);
        event.setAction(AirfieldEventAction.DAMAGE);

        AirfieldEventMatcher matcher = new AirfieldEventMatcher();
        matcher.setSide(Side.ALLIES);
        matcher.setAction(AirfieldEventAction.DAMAGE);
        matcher.setName("Rome");

        Assert.assertTrue(matcher.match(event));
    }

}
