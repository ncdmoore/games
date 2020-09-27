package engima.waratsea.viewmodel.squadrons;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.view.squadron.SquadronViewType;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NationSquadronsViewModel {
    private final Provider<SquadronsViewModel> squadronsViewModelProvider;
    private final Map<SquadronViewType, SquadronsViewModel> squadronTypes = new HashMap<>();

    @Getter private Nation nation;

    @Inject
    public NationSquadronsViewModel(final Provider<SquadronsViewModel> squadronsViewModelProvider) {
        this.squadronsViewModelProvider = squadronsViewModelProvider;

        Stream
                .of(SquadronViewType.values())
                .forEach(this::buildSquadronTypeViews);
    }

    /**
     * Set the nation's squadrons. Note, that a nation may not necessarily have squadrons.
     * Thus, the nation must be passed in as it is not always possible to derive the nation
     * from the squadrons.
     *
     * @param newNation The nation.
     * @param squadrons The nation's squadrons.
     */
    public void set(final Nation newNation, final List<Squadron> squadrons) {
        nation = newNation;
        squadronTypes.forEach((type, viewModel) -> viewModel.set(getSquadronsOfType(type, squadrons)));
    }

    /**
     * Get the squadron view model for the given type of squadron.
     *
     * @param type The squadron view type.
     * @return The squadrons view model for the given type.
     */
    public  SquadronsViewModel getType(final SquadronViewType type) {
        return squadronTypes.get(type);
    }

    private void buildSquadronTypeViews(final SquadronViewType type) {
        squadronTypes.put(type, squadronsViewModelProvider.get());
    }

    private List<Squadron> getSquadronsOfType(final SquadronViewType type, final List<Squadron> squadrons) {
        return squadrons
                .stream()
                .filter(s -> SquadronViewType.get(s.getType()) == type)
                .collect(Collectors.toList());
    }
}
