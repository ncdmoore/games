package engima.waratsea.model.enemy.views.port.data;

import engima.waratsea.model.base.port.Port;
import lombok.Data;

@Data
public class PortViewData {
    private String name;
    private transient Port port;
}
