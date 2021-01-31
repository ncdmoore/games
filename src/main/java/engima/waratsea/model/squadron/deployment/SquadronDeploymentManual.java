package engima.waratsea.model.squadron.deployment;

import com.google.inject.Inject;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameMap;
import engima.waratsea.model.player.Player;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;

import java.util.List;

public class SquadronDeploymentManual {
    private final GameMap gameMap;

    private Player player;
    private Side side;

    /**
     * Constructor called by guice.
     *
     * @param gameMap The game's map.
     */
    @Inject
    public SquadronDeploymentManual(final GameMap gameMap) {
        this.gameMap = gameMap;
    }

    /**
     * Deploy the human player's squadrons.
     *
     * @param gamePlayer The human player.
     */
    public void deploy(final Player gamePlayer) {
        this.player = gamePlayer;
        side = this.player.getSide();

        this
                .player
                .getNations()
                .stream()
                .filter(Nation::isSquadronsPresent)
                .forEach(this::deployNation);
    }

    /**
     * Set the regions minimum and maximum steps for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc ...
     */
    private void deployNation(final Nation nation) {
        List<Airfield> airfields = gameMap.getNationAirfields(side, nation);                  //Contains all the airfields for this nation.

        List<Squadron> squadrons = player.getSquadrons(nation, SquadronLocationType.LAND);

        airfields
                .stream()
                .map(airfield -> airfield.clearSquadrons(nation))
                .map(airfield -> airfield.getRegion(nation))
                .distinct()
                .forEach(region -> region.setRequirements(squadrons));
    }
}
