package engima.waratsea.viewmodel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.aircraft.LandingType;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;
import engima.waratsea.presenter.squadron.Deployment;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class DeploymentViewModel {
    @Getter private Map<Nation, ObjectProperty<ObservableList<Deployment>>> deployment = new HashMap<>();
    @Getter private Map<Nation, IntegerProperty> numLandingTypes = new HashMap<>();
    @Getter private Map<Nation, Map<Region, BooleanProperty>> regionMinimum = new HashMap<>();

    private Set<LandingType> landingTypes = Set.of(LandingType.LAND, LandingType.SEAPLANE);
    private Game game;
    private GameMap gameMap;

    /**
     * Constructor called by guice.
     *
     * @param game The game.
     * @param gameMap The game map.
     */
    @Inject
    public DeploymentViewModel(final Game game, final GameMap gameMap) {
        this.game = game;
        this.gameMap = gameMap;

        init();
    }

    /**
     * Initialize the deployment view model.
     */
    public void init() {
        deployment.clear();
        numLandingTypes.clear();

        game
                .getHumanPlayer()
                .getNations()
                .forEach(nation -> {
                    deployment.put(nation, new SimpleObjectProperty<>());
                    numLandingTypes.put(nation, new SimpleIntegerProperty(0));
                    regionMinimum.put(nation, getRegionMinimumMap(nation));
                });
    }

    /**
     * Set the model.
     **/
    public void setModel() {
        numLandingTypes.forEach((nation, prop) -> prop.set(0));
        deployment.forEach((nation, prop) -> prop.set(getNationsDeployment(nation)));
        numLandingTypes.forEach((nation, prop) -> prop.set(landingTypes.size()));
        regionMinimum.keySet().forEach(this::setRegionMinimumMap);
    }

    /**
     * Get a given nations deployment.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The given nation's deployment.
     */
    private ObservableList<Deployment> getNationsDeployment(final Nation nation) {
        List<Squadron> squadrons = game
                .getHumanPlayer()
                .getSquadrons(nation, SquadronLocationType.LAND);

        List<Deployment> rawDeployment = landingTypes
                .stream()
                .map(landingType -> getDeployment(landingType, squadrons))
                .collect(Collectors.toList());

        return  FXCollections.observableArrayList(rawDeployment);
    }

    /**
     * Get a landing type's deployment.
     *
     * @param landingType The landing type.
     * @param squadrons The squadrons for a given nation.
     * @return A landing type's deployment.
     */
    private Deployment getDeployment(final LandingType landingType, final List<Squadron> squadrons) {
        BigDecimal total = squadrons
                .stream()
                .filter(squadron -> squadron.isLandingTypeCompatible(landingType))
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deployed = squadrons
                .stream()
                .filter(squadron -> squadron.isLandingTypeCompatible(landingType))
                .filter(Squadron::isDeployed)
                .map(Squadron::getSteps)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Deployment landingTypeDeployment = new Deployment(landingType);
        landingTypeDeployment.setDeployedSteps(deployed);
        landingTypeDeployment.setTotalSteps(total);
        return landingTypeDeployment;
    }

    /**
     * Set the region's minimum requirement map for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A map of region to boolean property. This map just contains regions that have a minimum
     * squadron requirement.
     */
    private Map<Region, BooleanProperty> getRegionMinimumMap(final Nation nation) {
        return getRegionsWithMinimum(nation).stream().collect(Collectors.toMap(
                region -> region,
                region -> new SimpleBooleanProperty()));
    }

    /**
     * Set the given nation's region mininum requirements map.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     */
    private void setRegionMinimumMap(final Nation nation) {
        regionMinimum.get(nation).forEach((region, prop) -> prop.setValue(region.minimumSatisfied()));
    }

    /**
     * Get the regions with minimum squadron requirements.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return The regions that have a minimum squadron requirement.
     */
    private List<Region> getRegionsWithMinimum(final Nation nation) {
        return gameMap
                .getNationRegions(game.getHumanSide(), nation)
                .stream()
                .filter(Region::hasMinimumRequirement)
                .collect(Collectors.toList());
    }
}
