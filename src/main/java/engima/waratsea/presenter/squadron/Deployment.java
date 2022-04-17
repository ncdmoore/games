package engima.waratsea.presenter.squadron;

import engima.waratsea.model.aircraft.LandingType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Deployment {
    @Getter private final LandingType type;
    @Getter @Setter private int totalSteps;
    @Getter @Setter private int deployedSteps;
}
