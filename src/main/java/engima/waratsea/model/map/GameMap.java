package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionDAO;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.MinefieldDAO;
import engima.waratsea.model.scenario.Scenario;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Represents the game map. This is essentially the game board. It is not a GUI component.
 *
 */
@Slf4j
@Singleton
public final class GameMap {
    private static final int ALPHABET_SIZE = 26;

    private static final String MAP_REFERENCE_FORMAT = "\\s*([a-zA-Z]{1,2})(\\d{1,2})\\s*";
    private static final Pattern PATTERN = Pattern.compile(MAP_REFERENCE_FORMAT);
    public static final String ANY_ENEMY_BASE = "ANY_ENEMY_BASE";
    public static final String ANY_FRIENDLY_BASE = "ANY_FRIENDLY_BASE";

    private RegionDAO regionDAO;
    private MinefieldDAO minefieldDAO;

    @Getter
    private final int rows;

    @Getter
    private final int columns;

    private Map<Side, Set<Nation>> nations = new HashMap<>();

    private Map<Side, List<Region>> regions = new HashMap<>();

    private Map<Side, List<Airfield>> airfields = new HashMap<>();
    private Map<Side, Map<String, Airfield>> airfieldMap = new HashMap<>();   //Side to Map of Airfield name to Airfield.

    private Map<Side, List<Port>> ports = new HashMap<>();
    private Map<Side, Map<String, Port>> portMap = new HashMap<>();           //Side to Map of Port name to Port.

    private Map<Side, List<Minefield>> minefields = new HashMap<>();

    private Map<Side, Map<String, String>> baseRefToName = new HashMap<>();
    private Map<Side, Map<String, String>> baseNameToRef = new HashMap<>();

    private Map<Side, Map<Nation, List<Airfield>>> nationAirfieldMap = new HashMap<>();

    /**
     * The constructor of the GameMap. Called by guice.
     *
     * @param props Map properties.
     * @param regionDAO loads the region data.
     * @param minefieldDAO loads the minefield data.
     */
    @Inject
    public GameMap(final MapProps props,
                   final RegionDAO regionDAO,
                   final MinefieldDAO minefieldDAO) {
        this.regionDAO = regionDAO;
        this.minefieldDAO = minefieldDAO;

        rows = props.getInt("rows");
        columns = props.getInt("columns");
    }

    /**
     * Load the map.
     *
     * @param scenario The selected scenario.
     * @throws MapException An error occured attempting to load the map data.
     */
    public void load(final Scenario scenario) throws MapException {
        regions.put(Side.ALLIES, regionDAO.loadRegions(scenario, Side.ALLIES));
        regions.put(Side.AXIS, regionDAO.loadRegions(scenario, Side.AXIS));

        airfields.put(Side.ALLIES, buildAirfields(Side.ALLIES));
        airfields.put(Side.AXIS, buildAirfields(Side.AXIS));

        ports.put(Side.ALLIES, buildPorts(Side.ALLIES));
        ports.put(Side.AXIS, buildPorts(Side.AXIS));

        minefields.put(Side.ALLIES, minefieldDAO.load(Side.ALLIES));
        minefields.put(Side.AXIS, minefieldDAO.load(Side.AXIS));

        airfieldMap.put(Side.ALLIES, buildAirfieldMap(airfields.get(Side.ALLIES)));
        airfieldMap.put(Side.AXIS, buildAirfieldMap(airfields.get(Side.AXIS)));

        portMap.put(Side.ALLIES, buildPortMap(ports.get(Side.ALLIES)));
        portMap.put(Side.AXIS, buildPortMap(ports.get(Side.AXIS)));

        buildNationsMap(Side.ALLIES);
        buildNationsMap(Side.AXIS);

        buildNationAirfieldMap(Side.ALLIES);
        buildNationAirfieldMap(Side.AXIS);

        buildLocationToBaseMap(Side.ALLIES);
        buildLocationToBaseMap(Side.AXIS);
    }

    /**
     * Get the nations for the given side.
     *
     * @param side The side ALLIES or AXIS.
     * @return A set of nations for the given side.
     */
    public Set<Nation> getNations(final Side side) {
        return nations.get(side);
    }


    /**
     * Get a side's airfields.
     *
     * @param side The side ALLIES or AXIS.
     * @return The side's airfields.
     */
    public List<Airfield> getAirfields(final Side side) {
        return airfields.get(side);
    }

    /**
     * Get the an airfield by name.
     *
     * @param side The side ALLIES or AXIS.
     * @param airfieldName The name of the airfield to retrieve.
     * @return The airfield that corresponds to the given name.
     */
    public Airfield getAirfield(final Side side, final String airfieldName) {
        return airfieldMap.get(side).get(airfieldName);
    }

    /**
     * Get a side's ports.
     *
     * @param side The side ALLIES or AXIS.
     * @return The side's ports.
     */
    public List<Port> getPorts(final Side side) {
        return ports.get(side);
    }

    /**
     * Get the a port by name.
     *
     * @param side The side ALLIES or AXIS.
     * @param portName The name of the port to retrieve.
     * @return The port that corresponds to the given name.
     */
    public Port getPort(final Side side, final String portName) {
        return portMap.get(side).get(portName);
    }

    /**
     * Get all of the bases on this map.
     *
     * @return A list of all the game bases.
     */
    public List<Base> getBases() {
        return Stream.concat(getBases(Side.ALLIES), getBases(Side.AXIS))
                .distinct()
                .collect(toList());
    }

    /**
     * Get the given side's and nation's airfields.
     *
     * @param side The side ALLIES of AXIS.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's airfields.
     */
    public List<Airfield> getNationAirfields(final Side side, final Nation nation) {
        return  nationAirfieldMap.get(side).get(nation);
    }

    /**
     * Get the given side's minefields.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of the given side's minefields.
     */
    public List<Minefield> getMinefields(final Side side) {
        return minefields.get(side);
    }

    /**
     * Determine if the given location is a base for the given side.
     *
     * @param side The side ALLIES or AXIS.
     * @param location The map reference location.
     * @return True if the given location is a base for the given side.
     */
    public boolean isLocationBase(final Side side, final String location) {
        return baseRefToName.get(side).containsKey(convertNameToReference(location));
    }


    /**
     * Determine fi the given location is a base for either side.
     *
     * @param gameGrid A game map grid.
     * @return True if the grid corresponds to base. False otherwise.
     */
    public boolean isLocationBase(final GameGrid gameGrid) {
        String mapRef = convertGridToReference(gameGrid);

        for (Side side : Side.values()) {
            if (baseRefToName.get(side).containsKey(mapRef)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Convert a location name to a map reference. For example, the name Gibraltar is converted to H22.
     *
     * @param name A named location on the map.
     * @return The corresponding map reference of where the name is located.
     */
    public String convertNameToReference(final String name) {
        if (name == null) {
            log.error("Location is null");
            return null;
        }

        Matcher matcher = PATTERN.matcher(name);
        return   matcher.matches() ? name : Optional.ofNullable(getBaseReference(name)).orElse(name);
    }

    /**
     * Convert a map reference to a location name. For example, the reference H22 is converted to Gibraltar.
     *
     * @param reference A map reference.
     * @return The corresponding location name.
     */
    public String convertReferenceToName(final String reference) {
        return Optional.ofNullable(getBaseName(reference)).orElse(reference);
    }

    /**
     * Convert a game map reference into grid coordinates.
     *
     * @param mapReference game map reference.
     * @return a map grid coordinate.
     */
    public GameGrid getGrid(final String mapReference) {

        if (mapReference == null) {
            log.error("Cannot get grid. Map reference is null");
            return null;
        }

        Pattern pattern = Pattern.compile(MAP_REFERENCE_FORMAT);
        Matcher matcher = pattern.matcher(mapReference);

        int row = 0;
        int column = 0;

        while (matcher.find()) {
            String columnLabel = matcher.group(1);
            String rowLabel = matcher.group(2);
            row = Integer.parseInt(rowLabel) - 1;      // zero-based row numbers.
            column = convertColumn(columnLabel);       // zero-based column numbers.
        }


        log.debug("Map reference: {}", mapReference);
        log.debug("Grid row: {}, column: {}", row, column);

        return new GameGrid(row, column);
    }


    /**
     * Convert a game grid row and column into a game map reference.
     *
     * @param gameGrid The game grid. Contains a row and a column.
     * @return The game map reference.
     */
    public String convertGridToReference(final GameGrid gameGrid) {
        final int asciiA = 65;
        String mapRef;

        int row = gameGrid.getRow();
        int col = gameGrid.getColumn();

        int oneBasedRow = row + 1;
        int factor = col / ALPHABET_SIZE;
        int mod = col % ALPHABET_SIZE;

        if (factor == 0) {
            mapRef = Character.toString((char) (asciiA + col)) + oneBasedRow;
        } else {
            char first = (char) (asciiA - 1 + factor);
            char second = (char) (asciiA + mod);
            mapRef = first + Character.toString(second) + oneBasedRow;
        }

        return mapRef;
    }

    /**
     * Convert an array of numeric base 26 characters to an integer. For example, the column label AA is converted into
     * 26. The first A = 26, second A = 1. Thus, 26 + 1 = 27. But, since the columns are zero based AA is equal 26.
     *
     * @param columnLabel String that represents the column on the game map.
     * @return The integer equivalent of the given column string.
     */
    private int convertColumn(final String columnLabel) {

        char[] numericCharacters = columnLabel.toCharArray();

        int value = 0;

        for (char c : numericCharacters) {
            char base = 'a';
            if (Character.isUpperCase(c)) {
                base = 'A';
            }
            value = (value * ALPHABET_SIZE) + ((c - base) + 1);
        }

        return value - 1;
    }

    /**
     * Build a location map reference to base name map for the given side. Both ports and airfields are
     * included in this map.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildLocationToBaseMap(final Side side) {
        Map<String, String> refToNameMap = getBases(side)
                .distinct()
                .collect(Collectors.toMap(Base::getReference,
                                          Base::getName,
                                          (oldValue, newValue) -> newValue));

        Map<String, String> nameToRefMap = getBases(side)
                .distinct()
                .collect(Collectors.toMap(Base::getName,
                                          Base::getReference,
                                          (oldValue, newValue) -> newValue));

        baseRefToName.put(side, refToNameMap);
        baseNameToRef.put(side, nameToRefMap);
    }

    /**
     * Get a stream of all the bases both airfields and ports.
     *
     * @param side The side ALLIES or AXIS.
     * @return A stream of bases.
     */
    private Stream<Base> getBases(final Side side) {
        return Stream.concat(airfields.get(side).stream(), ports.get(side).stream());
    }

    /**
     * Build or get a list of all of the given side's airfields.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of the given side's airfields.
     */
    private List<Airfield> buildAirfields(final Side side) {
        return regions
                .get(side)
                .stream()
                .flatMap(region -> region.getAirfields().stream())
                .filter(distinctByKey(Airfield::getName))
                .collect(toList());
    }

    /**
     * Build or get a list of all of the given side's ports.
     *
     * @param side the side ALLIES or AXIS.
     * @return A list of the given side's ports.
     */
    private List<Port> buildPorts(final Side side) {
        return regions
                .get(side)
                .stream()
                .flatMap(region -> region.getPorts().stream())
                .filter(distinctByKey(Port::getName))
                .collect(toList());
    }

    /**
     * Get the base map reference given the base name.
     *
     * @param name The name of the base.
     * @return The map reference of the base.
     */
    private String getBaseReference(final String name) {
        String ref = baseNameToRef.get(Side.ALLIES).get(name);
        if (ref == null) {
            ref = baseNameToRef.get(Side.AXIS).get(name);
        }

        return ref;
    }

    /**
     * Get the base name given the base reference.
     *
     * @param reference The base's map reference.
     * @return The name of the base.
     */
    private String getBaseName(final String reference) {
        String name = baseRefToName.get(Side.ALLIES).get(reference);
        if (name == null) {
            name = baseRefToName.get(Side.AXIS).get(reference);
        }

        return name;
    }

    /**
     * Build the airfield name to airfield map.
     *
     * @param fields A list of airfields for a given side.
     * @return A map of airfield names to airfields.
     */
    private Map<String, Airfield> buildAirfieldMap(final List<Airfield> fields) {
        return fields
                .stream()
                .collect(Collectors.toMap(Airfield::getName, airfield -> airfield));
    }

    /**
     * Build the port name to port map.
     *
     * @param seaPorts A list of ports for a given side.
     * @return A map of port names to ports.
     */
    private Map<String, Port> buildPortMap(final List<Port> seaPorts) {
        return seaPorts
                .stream()
                .collect(Collectors.toMap(Port::getName, port -> port));
    }

    /**
     * Get the side's nations. These are the nations that have bases on the map.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildNationsMap(final Side side) {
        Set<Nation> nationSet = regions.get(side)
                .stream()
                .flatMap(region -> region.getNations().stream())
                .collect(Collectors.toSet());

        nations.put(side, nationSet);

    }

    /**
     * Build a map of a nation to airfield list.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildNationAirfieldMap(final Side side) {
        Map<Nation, List<Airfield>> map = new HashMap<>();

        nations
                .get(side)
                .forEach(nation -> map.put(nation, buildNationAirfields(side, nation)));

        nationAirfieldMap.put(side, map);
    }

    /**
     * Get the given nation's airfields.
     *
     * @param side The side ALLIES or AXIS that the nation is on.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's airfields.
     */
    private List<Airfield> buildNationAirfields(final Side side, final Nation nation) {
        return airfields.get(side)
                .stream()
                .filter(airfield -> airfield.usedByNation(nation))
                .collect(Collectors.toList());
    }

    /**
     * Predicate used to filter list of objects by an object property value.
     *
     * @param keyExtractor The function that gives the filtered property value.
     * @param <T> The type of object.
     * @return A predicate function.
     */
    private static <T> Predicate<T> distinctByKey(final Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}
