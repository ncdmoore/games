package engima.waratsea.model.game;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.scenario.Scenario;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * This class represents the game. It contains the game rules, game players etc.
 */
@Slf4j
@Singleton
public class Game {

    public static final String DEFAULT_GAME = "bombAlley";

    @Getter
    @Setter
    private String name;

    @Getter
    private Side side;

    private final Player computerPlayer;
    private final Player humanPlayer;

    @Getter
    @Setter
    private Scenario scenario;

    /**
     * The constructor for the game.
     *
     * @param humanPlayer The human player.
     * @param computerPlayer The computer player.
     */
    @Inject
    public Game(@Named("Human") final Player humanPlayer, @Named("Computer") final Player computerPlayer) {
        this.humanPlayer = humanPlayer;
        this.computerPlayer = computerPlayer;
        this.name = DEFAULT_GAME;
    }

    /**
     * Sets the sides of the two players of the game.
     *
     * @param side The human player side.
     */
    public void setSide(final Side side) {
        log.info("Human side: {}", side);

        this.side = side;
        humanPlayer.setSide(side);
        computerPlayer.setSide(side.opposite());
    }


}
