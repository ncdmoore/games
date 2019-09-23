package engima.waratsea.presenter.squadron;

import engima.waratsea.model.aircraft.LandingType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class Deployment {
    @Getter
    private final LandingType type;

    @Getter
    @Setter
    private BigDecimal totalSteps;

    @Getter
    @Setter
    private BigDecimal deployedSteps;

    /**
     * The constructor.
     *
     * @param type The type of deployment based on landing type.
     */
    public Deployment(final LandingType type) {
        this.type = type;
    }
}
