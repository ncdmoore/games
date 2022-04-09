package engima.waratsea.model.enemy.views.airfield.data;

import engima.waratsea.model.base.airfield.Airfield;
import lombok.Data;

@Data
public class AirfieldViewData {
    private String name;
    private transient Airfield airfield;
}
