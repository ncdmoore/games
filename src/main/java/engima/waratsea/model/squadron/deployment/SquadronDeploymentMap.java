package engima.waratsea.model.squadron.deployment;

import com.google.inject.Inject;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The squadron deployment map for a nation.
 */
@Slf4j
public class SquadronDeploymentMap {
    private GameMap gameMap;

    @Getter
    private Map<AircraftBaseType, Map<Integer, List<Airfield>>> deploymentMap;       //Contains all the airfields for a given nation.

    @Getter
    private Map<Airfield, List<String>> modelMap;                                    //Contains only deployment airfields for a given nation.

    private Side side;
    private Nation nation;

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game's map.
     */
    @Inject
    public SquadronDeploymentMap(final GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Build the deployment map.
     *
     * @param deploymentSide  The deployment's side.
     * @param deploymentNation The deployment's nation.
     * @param deployments The airfield deployment rankings.
     * @param airfields The airfields.
     */
    public void build(final Side deploymentSide, final Nation deploymentNation, final List<SquadronDeployment> deployments, final List<Airfield> airfields) {
        side = deploymentSide;
        nation = deploymentNation;
        deploymentMap = getRankingMap(deployments, airfields);
        modelMap = getModelMap(deployments, airfields);
    }

    /**
     * Get an airfield's mandatory models.
     *
     * @param airfield An airfield.
     * @return A list of mandatory aircraft models for the given airfield.
     */
    public List<String> getMandatoryModels(final Airfield airfield) {
        return Optional.ofNullable(modelMap.get(airfield))
                .orElse(Collections.emptyList());
    }

    /**
     * Get the ranking map for a given type of aircraft.
     *
     * @param type The base type of aircraft for which the ranking map is returned.
     * @return The ranking map for the given type of aircraft.
     */
    public List<Map.Entry<Integer, List<Airfield>>> getRankingForType(final AircraftBaseType type) {

        Map<Integer, List<Airfield>> rankingForTypeMap = deploymentMap.get(type);

        rankingForTypeMap
                .forEach((ranking, airfields) -> airfields
                        .forEach(airfield -> log.debug("{} Airfield: '{}', ranking: '{}'", new Object[]{type, airfield.getName(), ranking})));

        return rankingForTypeMap
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());
    }

    /**
     * Get a regional airfield ranking map. This is a ranking by aircraft type of just
     * the airfields in the given region.
     *
     * @param region A map region.
     * @return A map that maps the aircraft base type to a list of airfields in the given region.
     * The list is sorted by ranking. Meaning the first airfield in the list is the most
     * desirable airfield to deploy aircraft of the corresponding type.
     */
    public Map<AircraftBaseType, Map<Integer, List<Airfield>>> getRegionRankingMap(final Region region) {
        return deploymentMap
                .entrySet()
                .stream()
                .map(entry -> filterRegion(region, entry))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get the deployment map ranking for this nations airfields.
     *
     * @param deployments The airfield deployment rankings.
     * @param airfields The airfields.
     * @return A map of base aircraft type to airfield ranking. This airfield ranking includes
     * every airfield that this nation may use.
     */
    private Map<AircraftBaseType, Map<Integer, List<Airfield>>> getRankingMap(final List<SquadronDeployment> deployments,
                                                                              final List<Airfield> airfields) {
        // This will be a map of aircraft base type to all airfields for this nation.
        // Any airfield not mentioned in the deployment is simply added to the end
        // of the ranking.
        return Stream.of(AircraftBaseType.values())
                .collect(Collectors.toMap(type -> type, type -> getRanking(type, deployments, airfields)));
    }

    /**
     * Get the airfield to aircraft model map.
     *
     * @param deployments The airfield deployment.
     * @param airfields The airfields.
     * @return A map of airfield to list of aircraft models map.
     */
    private Map<Airfield, List<String>> getModelMap(final List<SquadronDeployment> deployments,
                                                    final List<Airfield> airfields) {
        return deployments
                .stream()
                .collect(Collectors.toMap(this::getAirfield, SquadronDeployment::getMandatory));
    }

    /**
     * Get the full airfield ranking for the given base aircraft type.
     *

     * @param type The base aircraft type.
     * @param deployments The airfield deployment rankings.
     * @param airfields The airfields.
     * @return A list of every airfield ranked by the desirability of deploying the given type of aircraft.
     */
    private Map<Integer, List<Airfield>> getRanking(final AircraftBaseType type,
                                                    final List<SquadronDeployment> deployments,
                                                    final List<Airfield> airfields) {
        // Get the airfields from the deployment. Sort them by ranking.
        Map<Integer, List<Airfield>> ranked = deployments
                .stream()
                .collect(Collectors.toMap(deployment -> deployment.getRanking(type),
                        this::getAirfieldList,
                        this::merge));

        List<Airfield> rankedAirfields = ranked
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());

        int lowestRanking = ranked.keySet().stream().mapToInt(v -> v).max().orElse(0);

        // We need to remove any zero ranked airfields as a zero ranking is invalid.
        // They will be re-added in the code below that adds all airfields without any ranking.
        List<Airfield> zeroRankedAirfields = new ArrayList<>();
        if (ranked.containsKey(0)) {
            zeroRankedAirfields = ranked.get(0);
            ranked.remove(0);
        }

        // Since the deployment may not mention all airfields, we add any
        // remaining airfield at the end of the list. This way we are assured that
        // the deployment contains all airfields.
        List<Airfield> nonRanked = airfields
                .stream()
                .filter(airfield -> !rankedAirfields.contains(airfield))
                .collect(Collectors.toList());

        if (!zeroRankedAirfields.isEmpty()) {
            nonRanked.addAll(zeroRankedAirfields);
        }

        ranked.put(lowestRanking + 1, nonRanked);

        // All airfields for this nation, ranked.
        return ranked;
    }

    /**
     * Convert the airfield name in the deployment to an actual airfield.
     *
     * @param deployment The squadron deployment.
     * @return The corresponding airfield of the deployment.
     */
    private Airfield getAirfield(final SquadronDeployment deployment) {
        return gameMap.getAirfield(side, deployment.getName());
    }

    /**
     * Convert the airfield names in the deployment to actual airfield objects.
     *
     * @param deployment The squadron deployment.
     * @return A list of airfields names.
     */
    private List<Airfield> getAirfieldList(final SquadronDeployment deployment) {
        Airfield airfield = getAirfield(deployment);
        List<Airfield> list = new ArrayList<>();
        list.add(airfield);
        return list;
    }

    /**
     * Merge two lists.
     *
     * @param oldValue A list of airfields.
     * @param newValue A list of airfields.
     * @return The combined list of airfields.
     */
    private List<Airfield> merge(final List<Airfield> oldValue, final List<Airfield> newValue) {
        oldValue.addAll(newValue);
        return oldValue;
    }


    /**
     * Filter the given airfield list by the given region.
     *
     * @param region A map region.
     * @param map A deployment map entry for a particular type of aircraft.
     * @return The updated entry with the list of airfields filtered by the given region.
     */
    private Map.Entry<AircraftBaseType, Map<Integer, List<Airfield>>> filterRegion(final Region region,
                                                                                   final Map.Entry<AircraftBaseType, Map<Integer, List<Airfield>>> map) {
        // Get the airfields that are only in the given region.
        Map<Integer, List<Airfield>> filtered = map.getValue().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> filterAirfield(region, entry.getValue())));

        return new AbstractMap.SimpleEntry<>(map.getKey(), filtered);
    }

    /**
     * Filter the given list of airfields by region. Only airfields within the given region are returned.
     *
     * @param region A map region.
     * @param fields A list of airfields.
     * @return A list of airfields that reside in the given region.
     */
    private List<Airfield> filterAirfield(final Region region, final List<Airfield> fields) {
        return fields
                .stream()
                .filter(airfield -> isAirfieldInRegion(region, airfield))
                .collect(Collectors.toList());
    }

    /**
     * Determine if the given airfield is within the given region.
     *
     * @param region A map region.
     * @param airfield An airfield.
     * @return True if the given airfield is in the given region.
     */
    private boolean isAirfieldInRegion(final Region region, final Airfield airfield) {
        return region.getAirfields().contains(airfield);
    }
}
