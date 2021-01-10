package engima.waratsea.model.base.airfield;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.mission.stats.ProbabilityStats;
import engima.waratsea.model.game.rules.AirOperationRules;
import engima.waratsea.model.squadron.state.SquadronAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class AirOperations {

    private final AirOperationRules rules;

    //private final Map<LandingType, AirbaseType> landingTypeMap = new HashMap<>();

    @Inject
    public AirOperations(final AirOperationRules rules) {
        this.rules = rules;

      //  landingTypeMap.put(LandingType.LAND, AirbaseType.LAND);
      //  landingTypeMap.put(LandingType.SEAPLANE, AirbaseType.SEAPLANE);
    }

   /* public void takeOff(final Squadron squadron) {
        AirbaseType airbaseType = squadron.getHome().getAirbaseType();

        // When the airfield type is both, look at squadron landing type to get airfield type.
        airbaseType = airbaseType == AirbaseType.BOTH ? landingTypeMap.get(squadron.getLandingType()) : airbaseType;

        // just a place holder until real function is written
        rules.getProbabilityCrash(airbaseType, SquadronAction.TAKE_OFF);
    }*/

    /**
     * Get the air operation stats for the given airbase.
     *
     * @param airbase The airbase.
     * @return The air operation stats for the given airbase.
     */
    public List<ProbabilityStats> getStats(final Airbase airbase) {
        List<AirbaseType> airbaseTypes = airbase.getAirbaseType().expand();

        return airbaseTypes
                .stream()
                .map(type -> {
                    ProbabilityStats stats = new ProbabilityStats();
                    stats.setTitle(type.getSquadronType());
                    stats.setEventColumnTitle("Step Destroyed");
                    stats.setProbability(buildStats(type));
                    return stats;
                }).collect(Collectors.toList());
    }

    private Map<String, Integer> buildStats(final AirbaseType airbaseType) {
        Map<String, Integer> stats = new HashMap<>();

        stats.put("On take-off", rules.getProbabilityCrash(airbaseType, SquadronAction.TAKE_OFF));
        stats.put("On landing", rules.getProbabilityCrash(airbaseType, SquadronAction.LAND));

        return stats;
    }
}
