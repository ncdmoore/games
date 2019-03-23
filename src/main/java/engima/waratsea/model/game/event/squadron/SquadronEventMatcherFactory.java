package engima.waratsea.model.game.event.squadron;


import engima.waratsea.model.game.event.squadron.data.SquadronMatchData;

/**
 * Factory used by guice to create squadron event matchers.
 */
public interface SquadronEventMatcherFactory {
    /**
     * Creates a squadron event matcher.
     *
     * @param data squadron event matcher data read from a JSON file.
     * @return A squadron event matcher initialized with the data from the JSON file.
     */
    SquadronEventMatcher create(SquadronMatchData data);
}
