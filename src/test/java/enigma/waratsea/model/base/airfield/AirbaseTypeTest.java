package enigma.waratsea.model.base.airfield;

import engima.waratsea.model.base.airfield.AirbaseType;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class AirbaseTypeTest {

    @Test
    public void testExpand() {
        List<AirbaseType> types = AirbaseType.LAND.expand();

        Assert.assertEquals(types, List.of(AirbaseType.LAND));

        types = AirbaseType.BOTH.expand();

        Assert.assertEquals(types, List.of(AirbaseType.LAND, AirbaseType.SEAPLANE));
    }
}
