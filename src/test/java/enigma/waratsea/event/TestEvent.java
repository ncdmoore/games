package enigma.waratsea.event;

import engima.waratsea.event.GameEvent;
import lombok.Getter;
import lombok.Setter;

public class TestEvent extends GameEvent {
    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String name;

    public TestEvent() {
        action = "Test";
    }
}
