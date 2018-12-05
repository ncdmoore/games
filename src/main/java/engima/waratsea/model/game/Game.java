package engima.waratsea.model.game;

import com.google.inject.Inject;
import engima.waratsea.event.DateEventFactory;
import engima.waratsea.event.ShipEvent;
import engima.waratsea.event.ShipEventFactory;
import engima.waratsea.model.scenario.ScenarioException;
import engima.waratsea.model.scenario.ScenarioLoader;
import engima.waratsea.model.ships.TaskForce;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * This class represents the game. It contains the game rules, game players etc.
 */
@Slf4j
@Singleton
public class Game {


    @Getter
    @Setter
    private String name;

    @Getter
    private Side humanSide;

    @Getter
    private final Player computerPlayer;

    @Getter
    private final Player humanPlayer;

    private final ScenarioLoader scenarioLoader;



    private ShipEventFactory shipEventFactory;
    private DateEventFactory dateEventFactory;

    @Getter
    @Setter
    private Scenario scenario;

    /**
     * The constructor for the game.
     *
     * @param humanPlayer The human player.
     * @param computerPlayer The computer player.
     * @param scenarioLoader Loads player task forces.
     */
    @Inject
    public Game(@Named("Human") final Player humanPlayer,
                @Named("Computer") final Player computerPlayer,
                final ScenarioLoader scenarioLoader,
                final ShipEventFactory shipEventFactory,
                final DateEventFactory dateEventFactory) {
        this.humanPlayer = humanPlayer;
        this.computerPlayer = computerPlayer;
        this.scenarioLoader = scenarioLoader;
        this.shipEventFactory = shipEventFactory;
        this.dateEventFactory = dateEventFactory;
    }

    /**
     * Initialize the scenario summary data.
     * @return List of scenarios.
     * @throws ScenarioException Indicates the scenario summary data could not be loaded.
     */
    public List<Scenario> initScenarios() throws ScenarioException {                                                    // New Game Step 1.
        return scenarioLoader.loadSummaries();
    }

    /**
     * Sets the sides of the two players of the game.
     * @param side The human player humanSide.
     */
    public void setHumanSide(final Side side) {                                                                         // New Game Step 2.
        log.info("Human side: {}", side);

        humanSide = side;
        humanPlayer.setSide(humanSide);
        computerPlayer.setSide(humanSide.opposite());
    }

    /**
     * Initialize the task force data for both players.
     * @throws ScenarioException Indicates the scenario data could not be loaded.
     */
    public void initTaskForces() throws ScenarioException {                                                             // New Game Step 3.
        String scenarioName = scenario.getName();

        List<TaskForce> taskForces = scenarioLoader.loadTaskForce(scenarioName, humanSide);
        humanPlayer.setTaskForces(taskForces);
        taskForces = scenarioLoader.loadTaskForce(scenarioName, humanSide.opposite());
        computerPlayer.setTaskForces(taskForces);


        ShipEvent s = shipEventFactory.create();
        s.setAction("Spotted");
        s.setName("Bismarck");

        s.fire();

        ShipEvent x = shipEventFactory.create();
        x.setAction("Sunk");
        x.setName("hood");

        x.fire();

    }
}
