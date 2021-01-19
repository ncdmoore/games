package engima.waratsea.viewmodel.squadrons;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Game;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.SquadronLocationType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SideSquadronsViewModel {
    private final Game game;
    private final Provider<NationSquadronsViewModel> nationSquadronsViewModelProvider;
    private final Map<Nation, NationSquadronsViewModel> nationsSquadronsViewModel = new HashMap<>();

    private Side side;
    private SquadronLocationType locationType;


    @Inject
    public SideSquadronsViewModel(final Game game,
                                  final Provider<NationSquadronsViewModel> nationSquadronsViewModelProvider) {
        this.game = game;
        this.nationSquadronsViewModelProvider = nationSquadronsViewModelProvider;
    }

    /**
     * Set the view model's data from the model.
     *
     * @param newSide The side: ALLIES or AXIS.
     * @param newLocationType The location of the squadrons: LAND or SEA.
     */
    public void set(final Side newSide, final SquadronLocationType newLocationType) {
        side = newSide;
        locationType = newLocationType;

        //Build the nation's squadron view model. This cannot be done until we
        //know the side since the side specifies the nations.
        nationsSquadronsViewModel.clear();

        game
                .getPlayer(side)
                .getSquadronNations(locationType)
                .forEach(this::buildNationSquadronViewModel);
    }

    /**
     * Get the nations.
     *
     * @return The side's nations.
     */
    public Set<Nation> getNations() {
        return nationsSquadronsViewModel.keySet();
    }

    /**
     * Get the nation squadrons view model for the given nation.
     *
     * @param nation The nation.
     * @return The nation's squadrons view model for the given nation.
     */
    public NationSquadronsViewModel getNationViewModel(final Nation nation) {
        return nationsSquadronsViewModel.get(nation);
    }

    private void buildNationSquadronViewModel(final Nation nation) {
        List<Squadron> nationsSquadrons = game.getPlayer(side).getSquadrons(nation, locationType);                      //The nations squadrons.
        NationSquadronsViewModel nationViewModel = nationSquadronsViewModelProvider.get();
        nationViewModel.set(nation, nationsSquadrons);
        nationsSquadronsViewModel.put(nation, nationViewModel);
    }
}
