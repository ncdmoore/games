package engima.waratsea.model.enemy.views.port.data;

import engima.waratsea.model.base.port.Port;
import lombok.Getter;
import lombok.Setter;

public class PortViewData {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private transient Port port;
}
