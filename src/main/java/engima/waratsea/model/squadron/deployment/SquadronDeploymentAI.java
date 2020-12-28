package engima.waratsea.model.squadron.deployment;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.AircraftBaseType;
import engima.waratsea.model.base.airfield.AirfieldOperation;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronException;
import engima.waratsea.model.squadron.SquadronLocationType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements all of the squadron artificial intelligence.
 */
@Singleton
@Slf4j
public class SquadronDeploymentAI {

    private final GameMap gameMap;
    private final SquadronDeploymentDAO deploymentDAO;
    private final SquadronDeploymentMap deploymentMap;                                     //Contains all the airfields for this nation.

    private Scenario scenario;
    private Player player;
    private Side side;

    private List<Squadron> squadrons;                                                //Contains all the squadrons for this nation.
    private Map<AircraftBaseType, List<Squadron>> squadronTypeMap;
    private Map<String, List<Squadron>> squadronModelMap;

    private AircraftBaseType baseType = AircraftBaseType.BOMBER;                     //Seed the base aircraft type.

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game's map.
     * @param deploymentDAO The squadron deployment DAO.
     * @param deploymentMap The squadron deployment map.
     */
    @Inject
    public SquadronDeploymentAI(final GameMap gameMap, final SquadronDeploymentDAO deploymentDAO, final SquadronDeploymentMap deploymentMap) {
        this.gameMap = gameMap;
        this.deploymentDAO = deploymentDAO;
        this.deploymentMap = deploymentMap;
    }

    /**
     * Deploy the computer player's squadrons.
     *
     * @param selectedScenario The selected scenario.
     * @param gamePlayer The computer player.
     */
    public void deploy(final Scenario selectedScenario, final Player gamePlayer) {
        scenario = selectedScenario;
        player = gamePlayer;
        side = player.getSide();

        player.getNations().forEach(this::deployNation);
    }

    /**
     * Deploy the squadrons for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void deployNation(final Nation nation) {
        log.info("Deploy {} squadrons", nation);

        try {
            List<Airfield> airfields = gameMap.getNationAirfields(side, nation);                  //Contains all the airfields for this nation.
            List<SquadronDeployment> deployments = deploymentDAO.load(scenario, side, nation);    //The deployment for this nation.

            squadrons = new ArrayList<>();                                                        //We copy the player's squadrons since we will be removing squadrons from it
                                                                                                  //in order to deploy them.

            squadrons.addAll(player.getSquadrons(nation, SquadronLocationType.LAND));             //The squadrons for this nation.

            squadronTypeMap = getSquadronTypeMap();                                               //Map of aircraft base type to squadron.
            squadronModelMap = getSquadronModelMap();                                             //Map of aircraft model to squadron.

            deploymentMap.build(side, deployments, airfields);                                    //Build the nation's deployment.

            airfields
                    .stream()
                    .map(airfield -> airfield.clearSquadrons(nation))
                    .map(airfield -> airfield.getRegion(nation))
                    .distinct()
                    .map(region -> region.setRequirements(squadrons))
                    .filter(Region::hasMinimumRequirement)
                    .map(this::meetMandatory)
                    .forEach(this::meetMinimum);

            finishDeployment();

        } catch (SquadronException ex) {
            log.error("Unable to deploy squadrons. Unable to load deployment {} {}", scenario.getTitle(), nation);
        }
    }

    /**
     * Get the squadron type map. A map of base aircraft type to squadron list. This is used in the region
     * requirement fulfillment to evenly distribute the type of aircraft deployed.
     *
     * @return A map of base aircraft type to a list squadrons of that type.
     */
    private Map<AircraftBaseType, List<Squadron>> getSquadronTypeMap() {
        return squadrons
                .stream()
                .collect(Collectors.groupingBy(Squadron::getBaseType));
    }

    /**
     * Get the squadron model map. A map of aircraft model to squadron list. This is used in the region
     * requirement fulfillment when the deployment specifies a particular aircraft model must be deployed
     * to a particular airfield.
     *
     * @return A map of aircraft model to list of squqdrons of that model.
     */
    private Map<String, List<Squadron>> getSquadronModelMap() {
        return squadrons
                .stream()
                .collect(Collectors.groupingBy(Squadron::getModel));
    }

    /**
     * Ensure that the region requirements are met.
     *
     * @param region A map region.
     */
    private void meetMinimum(final Region region) {
        log.info("Meet minimum. Deploy region '{}' with needed requirement in steps: '{}'", region.getName(), region.getNeeded());

        region.getAirfields().forEach(airfield -> log.debug("Region '{}', airfield: '{}'", region.getName(), airfield.getName()));

        // remove the number of squadrons needed from the squadrons list.
        List<Squadron> deployed = getNeededSquadrons(region);

        // get the airfields in this region and their associated ranking. The airfield ranking is used to ensure
        // that airfields with the highest rankings get the squadrons.
        Map<AircraftBaseType, Map<Integer, List<Airfield>>> regionRankingMap = deploymentMap.getRegionRankingMap(region);

        deployed.forEach(squadron -> {
            AircraftBaseType type = squadron.getType().getBaseType();

            // Get the region's ranked airfields for the given type of squadron.
            Map<Integer, List<Airfield>> regionRankingMapForType = regionRankingMap.get(type);

            regionRankingMapForType
                    .forEach((ranking, airfields) -> airfields
                            .forEach(airfield -> log.debug("{} Airfield: '{}', ranking: '{}'", new Object[]{type, airfield.getName(), ranking})));

            // We could cash this and not sort it for every squadron. But, in most cases
            // the minimum requirement for a region is less than four squadrons;
            // thus, it is more efficient to just sort the type as each squadron is placed
            // rather than sorting ahead of time. This way we will do less sorting.
            List<Map.Entry<Integer, List<Airfield>>> sortedByRankings = regionRankingMapForType
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            deploySquadron(squadron, sortedByRankings);
        });
    }

    /**
     * Attempt to meet a region's minimum requirement with any mandatory squadron deployment of the airfields within
     * the region.
     *
     * @param region A map region.
     * @return The same map region.
     */
    private Region meetMandatory(final Region region) {
        log.info("Meet mandatory. Deploy region '{}' with needed requirement in steps: '{}'", region.getName(), region.getNeeded());

        region.getAirfields()
                .forEach(airfield -> deployMandatorySquadrons(airfield, region));

        return region;
    }

    /**
     * Deploy the mandatory squadrons.
     *
     * @param airfield An airfield.
     * @param region A map region.
     */
    private void deployMandatorySquadrons(final Airfield airfield, final Region region) {

        List<String> models = deploymentMap.getMandatoryModels(airfield); // List of models that are requested to be deployed.

        log.info("Airfield: '{}' requires models: '{}'", airfield.getName(), String.join(",", models));

        List<String> modelsDeployed = new ArrayList<>();   // List of models that are deployed.

        for (String model: models) {
            if (region.getNeeded() > 0) {
                deploySquadron(model, airfield);
                modelsDeployed.add(model);
            } else {
                return;
            }
        }

        // Remove the deployed models from the deployment map as they are now deployed
        // and we don't want to redeploy then when the finish mandatory method runs
        // later.
        removeDeployed(airfield, modelsDeployed);
    }

    /**
     *  Remove the deployed models from the deployment map so we don't deploy them
     *  a second time when the finish mandatory method runs.
     *
     * @param airfield The airfield.
     * @param deployed The squadrons deployed at the airfield.
     */
    private void removeDeployed(final Airfield airfield, final List<String> deployed) {
        List<String> models = deploymentMap.getMandatoryModels(airfield); // List of models that are requested to be deployed.

        deployed.forEach(models::remove);
    }

    /**
     * Deploy a mandatory squadron.
     *
     * @param model The aircraft model that is deployed.
     * @param airfield The airfield in which the squadron is deployed.
     */
    private void deploySquadron(final String model, final Airfield airfield) {
        try {
            Squadron squadron = squadronModelMap.get(model).remove(0);
            squadrons.remove(squadron);
            AirfieldOperation result = airfield.addSquadron(squadron);
            if (result == AirfieldOperation.SUCCESS) {
                log.info("Deploy squadron: '{} of type '{}' to airfield: '{}' result: '{}'", new Object[]{squadron.getName(), squadron.getBaseType(), airfield.getName(), result});
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error("Could not get find squadron of model '{}' to deploy the region mandatory requirement", model);
        }
    }

    /**
     * Get the squadrons to satisfy the given region's minimum squadron requirement.
     *
     * @param region A map region.
     * @return A list of squadrons.
     */
    private List<Squadron> getNeededSquadrons(final Region region) {
        List<Squadron> deployed = new ArrayList<>();
        int deployedSteps = 0;
        try {
            while (deployedSteps < region.getNeeded()) {
                Squadron squadron = removeSquadron();
                deployed.add(squadron);
                deployedSteps += squadron.getStrength().getSteps().intValue();
            }
        } catch (SquadronException ex) {
            log.error("Could not get enough squadrons to deploy the region requirement for region: " + region.getName());
        }

        return deployed;
    }

    /**
     * Finish deploying the remaining squadrons solely based on the deployment map.
     */
    private void finishDeployment() {
        finishMandatory();
        finishRemaining();

        if (squadrons.isEmpty()) {
            log.info("Deployment finished - success: true");
        } else {
            log.error("Deployment finished - success: false");
        }
    }

    /**
     * Finish any remaining deployment. Typically, most of the deployment is done here for bomb alley as it
     * has a random number of squadrons every game. Other games that have a fixed number of squadrons have
     * most of there squadrons deployed earlier.
     */
    private void finishRemaining() {
        log.info("Finish remaining");

        // This map is a cache of the sorted deployment rankings per base aircraft type.
        Map<AircraftBaseType, List<Map.Entry<Integer, List<Airfield>>>> rankings = new HashMap<>();

        try {
            // Use a while loop so we can very the type of squadron selected from the squadron list.
            // This is done to spread out the type's of squadrons among airfields that have a high
            // ranking. This avoids a situation where an airfield with a high ranking gits filled up
            // with aircraft of all the same type.
            while (!squadrons.isEmpty()) {
                Squadron squadron = removeSquadron();
                AircraftBaseType type = squadron.getType().getBaseType();

                List<Map.Entry<Integer, List<Airfield>>> sortedByRankings = rankings.containsKey(type)
                        ? rankings.get(type) : deploymentMap.getRankingForType(type);

                log.debug("Deploy a squadron with type: '{}'", type);

                sortedByRankings
                        .forEach(entry -> entry.getValue()
                                .forEach(airfield -> log.debug("{} Airfield: '{}', ranking: '{}'",
                                        new Object[]{type, airfield.getName(), entry.getKey()})));

                rankings.put(type, sortedByRankings);

                deploySquadron(squadron, sortedByRankings);
            }
        } catch (SquadronException ex) {
            log.error("Finish squadrons encountered an error.");
        }
    }

    /**
     * Finish the mandatory squadron deployment.
     */
    private void finishMandatory() {
        log.info("Finish mandatory.");

        deploymentMap.getModelMap()
                .forEach(((airfield, models) -> models.forEach(model -> deploySquadron(model, airfield))));

        //To handle half strength squadrons we will need a "Half" Strength model map.
    }

    /**
     * Deploy a squadron.
     *
     * @param squadron The squadron to deploy.
     * @param sortedByRankings The list form of the region ranking map sorted by ranking.
     */
    private void deploySquadron(final Squadron squadron, final List<Map.Entry<Integer, List<Airfield>>> sortedByRankings) {
        // Loop through the airfield list by deployment ranking.
        // Look for an airfield that can hold the squadron.
        // Airfields with a lower (more desirable ranking) are considered first.
        for (Map.Entry<Integer, List<Airfield>> entry : sortedByRankings) {
            List<Airfield> fields = entry.getValue();
            for (Airfield airfield : entry.getValue()) {
                // If the airfield has room add the squadron.
                AirfieldOperation result = airfield.addSquadron(squadron);
                log.info("Deploy squadron: '{}' of type '{}' to airfield: '{}' result: '{}'",
                        new Object[]{squadron.getName(),  squadron.getBaseType(), airfield.getName(), result});
                if (result == AirfieldOperation.SUCCESS) {
                    fields.remove(airfield); // Remove the airfield and place it at the end of the list.
                    fields.add(airfield);    // This give us a round robin deployment scheme for airfields at
                    return;                  // the same ranking.
                }
            }
        }
    }

    /**
     * Get a squadron to deploy.
     *
     * @return A squadron.
     * @throws SquadronException If there are no available squadrons to remove.
     */
    private Squadron removeSquadron() throws SquadronException  {
        baseType = baseType.next();    //Rotate through the aircraft types to ensure that an even distribution of
                                       //aircraft types are deployed.
        try {
            Squadron squadron = squadronTypeMap.get(baseType).remove(0);
            squadrons.remove(squadron);
            return squadron;               //The squadron to be deployed.
        } catch (IndexOutOfBoundsException | NullPointerException ex) {    // An empty list or no list at all.
            return tryAnotherType();                                       // No list happens when aircraft of a particular
        }                                                                  // type are not present.
    }

    /**
     * This is called when we try and get a squadron of a particular type in which there are no remaining
     * squadrons of that type.
     *
     * @return A squadron.
     * @throws SquadronException Thrown when there are no more squadrons.
     */
    private Squadron tryAnotherType() throws SquadronException {
        baseType = anySquadronsLeft();
        Squadron squadron = squadronTypeMap.get(baseType).remove(0);
        squadrons.remove(squadron);
        return squadron;
    }

    /**
     * Determine if there are any remaining squadrons in the squadron type map.
     * @return A type for which there are remaining squadrons.
     * @throws SquadronException Thrown when there are no more squadrons.
     */
    private AircraftBaseType anySquadronsLeft() throws SquadronException {
        for (AircraftBaseType type: AircraftBaseType.values()) {
            if (squadronTypeMap.get(type) != null && !squadronTypeMap.get(type).isEmpty()) {
                return type;
            }
        }

        throw new SquadronException("Deployment ran out of squadrons. Side:" + side + " type:" + baseType);
    }

}
