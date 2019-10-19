package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CapPatrol implements Patrol {

    private List<Squadron> squadrons;

    /**
     * The constructor.
     *
     * @param data The CAP patrol data read in from a JSON file.
     */
    public CapPatrol(final PatrolData data) {

        Map<String, Squadron> squadronMap = getSquadronMap(data.getAirbase().getSquadrons());

        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronMap::get)
                .collect(Collectors.toList());
    }
    /**
     * Get the Patrol data.
     *
     * @return The Patrol data.
     */
    @Override
    public PatrolData getData() {
        PatrolData data = new PatrolData();

        List<String> names = Optional
                .ofNullable(squadrons)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(Squadron::getName)
                .collect(Collectors.toList());

        data.setSquadrons(names);

        return  data;
    }

    /**
     * Get the squadrons on patrol.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on the patrol.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Add a squadron to the patrol.
     *
     * @param squadron The squadron added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
        squadrons.add(squadron);
        SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_PATROL);
        squadron.setSquadronState(state);
    }

    /**
     * Remove a squadron from the patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
        SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
        squadron.setSquadronState(state);
    }

    /**
     * Get a squadron map to aid in determine which squadrons of the airfield
     * are on CAP patrol.
     *
     * @param squadronList The airfield squadrons.
     * @return A map of squadron name to squadron.
     */
    private Map<String, Squadron> getSquadronMap(final List<Squadron> squadronList) {
        return squadronList
                .stream()
                .collect(Collectors.toMap(Squadron::getName, squadron -> squadron));
    }
}
