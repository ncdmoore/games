package engima.waratsea.model.base.airfield.patrol.rules;

import com.google.inject.name.Named;

public interface AirRulesFactory {
    /**
     * Creates air search rules.
     *
     * @return The air search rules.
     */
    @Named("search")
    AirRules createSearch();

    /**
     * Creates air ASW rules.
     *
     * @return Air ASW rules.
     */
    @Named("asw")
    AirRules createAsw();

    /**
     * Creates air CAP rules.
     *
     * @return air CAP rules.
     */
    @Named("cap")
    AirRules createCap();

}
