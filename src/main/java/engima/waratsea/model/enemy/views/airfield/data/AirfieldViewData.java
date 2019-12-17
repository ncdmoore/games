package engima.waratsea.model.enemy.views.airfield.data;

import engima.waratsea.model.base.airfield.Airfield;
import lombok.Getter;
import lombok.Setter;

public class AirfieldViewData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private transient Airfield airfield;
}
