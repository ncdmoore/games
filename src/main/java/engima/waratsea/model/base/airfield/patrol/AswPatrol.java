package engima.waratsea.model.base.airfield.patrol;

import engima.waratsea.model.base.Airbase;
import engima.waratsea.model.base.airfield.patrol.data.PatrolData;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.squadron.PatrolType;
import engima.waratsea.model.squadron.Squadron;
import engima.waratsea.model.squadron.state.SquadronAction;
import engima.waratsea.model.squadron.state.SquadronState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents an airbase's ASW patrol.
 *
 * An airbase may be an airfield or an aircraft/seaplane carrier.
 *
 */

@Slf4j
public class AswPatrol implements Patrol {
    private List<Squadron> squadrons;

    @Getter
    private final Airbase airbase;

    @Getter
    private int maxRadius;

    /**
     * The constructor.
     *
     * @param data The ASW patrol data read in from a JSON file.
     */
    public AswPatrol(final PatrolData data) {
        airbase = data.getAirbase();

        Map<String, Squadron> squadronMap = getSquadronMap(data.getAirbase().getSquadrons());

        squadrons = Optional.ofNullable(data.getSquadrons())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(squadronMap::get)
                .collect(Collectors.toList());

        updateMaxRadius();
    }

    /**
     * Get the ASW patrol data that is written to a JSON file.
     *
     * @return The persistent ASW patrol data.
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
     * Get the list of squadrons on ASW patrol for the given nation.
     *
     * @param nation The nation: BRITISH, ITALIAN, etc.
     * @return A list of squadrons on ASW patrol for the given nation.
     */
    @Override
    public List<Squadron> getSquadrons(final Nation nation) {
        return squadrons
                .stream()
                .filter(squadron -> squadron.ofNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Add a squadron to the ASW patrol.
     *
     * @param squadron The squadron that is added to the patrol.
     */
    @Override
    public void addSquadron(final Squadron squadron) {
        if (canAdd(squadron)) {   //Make sure the squadron is actuall deployed at the airbase.
            squadrons.add(squadron);
            SquadronState state = squadron.getSquadronState().transition(SquadronAction.ASSIGN_TO_PATROL);
            squadron.setSquadronState(state);
            updateMaxRadius();
        } else {
            log.error("Unable to add squadron: '{}' to patrol. Squadron not deployed to airbase: '{}'", squadron, airbase);
        }
    }

    /**
     * Remove a squadron from the ASW patrol.
     *
     * @param squadron The squadron removed from the patrol.
     */
    @Override
    public void removeSquadron(final Squadron squadron) {
        squadrons.remove(squadron);
        SquadronState state = squadron.getSquadronState().transition(SquadronAction.REMOVE_FROM_PATROL);
        squadron.setSquadronState(state);
        updateMaxRadius();
    }

    /**
     * Get a squadron map to aid in determine which squadrons of the airfield
     * are on ASW patrol.
     *
     * @param squadronList The airfield squadrons.
     * @return A map of squadron name to squadron.
     */
    private Map<String, Squadron> getSquadronMap(final List<Squadron> squadronList) {
        return squadronList
                .stream()
                .collect(Collectors.toMap(Squadron::getName, squadron -> squadron));
    }

    /**
     * Determine if the squadron may be added to the patrol.
     *
     * @param squadron The squadron that is potentially added to the patrol.
     * @return True if the given squadron may be added to this patrol. False otherwise.
     */
    private boolean canAdd(final Squadron squadron) {
        return squadron.canDoPatrol(PatrolType.ASW) && airbase.getSquadrons().contains(squadron);
    }

    /**
     * Update this search's maximum search radius. If the newly added squadron has a greater
     * readius then the current maximum search radius, then this squadron's search radius
     * is the new maximum search radius.
     *
     */
    private void updateMaxRadius() {
        maxRadius = squadrons
                .stream()
                .flatMap(s -> s.getRadius().stream())
                .max(Integer::compare)
                .orElse(0);

    }
}
