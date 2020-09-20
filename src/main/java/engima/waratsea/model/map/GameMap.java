package engima.waratsea.model.map;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import engima.waratsea.model.base.Base;
import engima.waratsea.model.base.airfield.Airfield;
import engima.waratsea.model.base.port.Port;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.region.Region;
import engima.waratsea.model.map.region.RegionDAO;
import engima.waratsea.model.map.region.RegionGrid;
import engima.waratsea.model.minefield.Minefield;
import engima.waratsea.model.minefield.MinefieldDAO;
import engima.waratsea.model.scenario.Scenario;
import engima.waratsea.model.taskForce.TaskForce;
import javafx.util.Pair;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.ArrayList;
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
    private static final String MAP_REFERENCE_FORMAT = "\\s*([a-zA-Z]{1,2})(\\d{1,2})\\s*";
    private static final Pattern PATTERN = Pattern.compile(MAP_REFERENCE_FORMAT);
    public static final String ANY_ENEMY_BASE = "ANY_ENEMY_BASE";
    public static final String ANY_FRIENDLY_BASE = "ANY_FRIENDLY_BASE";

    private final String defaultGridType;

    private final RegionDAO regionDAO;
    private final MinefieldDAO minefieldDAO;
    private final Provider<BaseGrid> baseGridProvider;
    private final Provider<TaskForceGrid> taskForceGridProvider;
    private final Provider<RegionGrid> regionGridProvider;

    @Getter private final int rows;
    @Getter private final int columns;

    private final MultiKeyMap<Integer, GameGrid> gridMap = new MultiKeyMap<>();          //Row, Column to Game grid map.
    private final Map<String, GameGrid> gridRefMap = new HashMap<>();                    //Map reference to Game grid map.

    private final Map<Side, Set<Nation>> nations = new HashMap<>();
    private final Map<Side, List<Region>> regions = new HashMap<>();

    private final Map<Side, List<Airfield>> airfields = new HashMap<>();
    private final Map<Side, Map<String, Airfield>> airfieldMap = new HashMap<>();        //Inner map: maps Airfield name to Airfield.

    private final Map<Side, List<Port>> ports = new HashMap<>();
    private final Map<Side, Map<String, Port>> portMap = new HashMap<>();                //Inner map: maps Port name to Port.

    private final Map<Side, List<Minefield>> minefields = new HashMap<>();

    private final Map<Side, Map<String, String>> portRefToName = new HashMap<>();        //Inner map: maps Port map reference to Port name.
    private final Map<Side, Map<String, String>> airfieldRefToName = new HashMap<>();    //Inner map: maps Airfield map reference to Airfield name.

    private final Map<Side, Map<String, BaseGrid>> baseRefToBase = new HashMap<>();      //Inner map: maps map reference to base grid.

    private final Map<Side, Map<String, String>> baseNameToRef = new HashMap<>();        //Inner map: maps Base name to Base map reference.

    private final Map<Side, Map<Nation, List<Region>>> nationRegionMap = new HashMap<>();
    private final Map<Side, Map<Nation, List<Airfield>>> nationAirfieldMap = new HashMap<>();

    @Getter private final Map<Side, List<TaskForceGrid>> taskForceGrids = new HashMap<>();

    private final Map<Side, Map<String, RegionGrid>> regionRefToRegion = new HashMap<>();

    /**
     * The constructor of the GameMap. Called by guice.
     *
     * @param props Map properties.
     * @param regionDAO loads the region data.
     * @param minefieldDAO loads the minefield data.
     * @param baseGridProvider Provides base grids.
     * @param taskForceGridProvider Provides task force grids.
     * @param regionGridProvider Provides region grids.
     */
    @Inject
    public GameMap(final MapProps props,
                   final RegionDAO regionDAO,
                   final MinefieldDAO minefieldDAO,
                   final Provider<BaseGrid> baseGridProvider,
                   final Provider<TaskForceGrid> taskForceGridProvider,
                   final Provider<RegionGrid> regionGridProvider) {
        this.regionDAO = regionDAO;
        this.minefieldDAO = minefieldDAO;
        this.baseGridProvider = baseGridProvider;
        this.taskForceGridProvider = taskForceGridProvider;
        this.regionGridProvider = regionGridProvider;

        rows = props.getInt("rows");
        columns = props.getInt("columns");

        defaultGridType = props.getString("defaultGridType", "LAND");

        buildGrid(props);


    }

    /**
     * Load the map.
     *
     * @param scenario The selected scenario.
     * @throws MapException An error occurred attempting to load the map data.
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

        nationRegionMap.put(Side.ALLIES, buildRegionMap(Side.ALLIES));
        nationRegionMap.put(Side.AXIS, buildRegionMap(Side.AXIS));

        airfieldMap.put(Side.ALLIES, buildAirfieldMap(airfields.get(Side.ALLIES)));
        airfieldMap.put(Side.AXIS, buildAirfieldMap(airfields.get(Side.AXIS)));

        portMap.put(Side.ALLIES, buildPortMap(ports.get(Side.ALLIES)));
        portMap.put(Side.AXIS, buildPortMap(ports.get(Side.AXIS)));

        nations.put(Side.ALLIES, buildNationsMap(Side.ALLIES));
        nations.put(Side.AXIS, buildNationsMap(Side.AXIS));

        nationAirfieldMap.put(Side.ALLIES, buildNationAirfieldMap(Side.ALLIES));
        nationAirfieldMap.put(Side.AXIS, buildNationAirfieldMap(Side.AXIS));

        buildRegionRefToRegionMap(Side.ALLIES);
        buildRegionRefToRegionMap(Side.AXIS);

        buildLocationToBaseMap(Side.ALLIES);
        buildLocationToBaseMap(Side.AXIS);

        taskForceGrids.put(Side.ALLIES, new ArrayList<>());
        taskForceGrids.put(Side.AXIS, new ArrayList<>());
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
    public Optional<Airfield> getAirfield(final Side side, final String airfieldName) {
        return Optional.ofNullable(airfieldMap.get(side).get(airfieldName));
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
     * @param location The name of the port to retrieve.
     * @return An optional port that corresponds to the given name.
     */
    public Optional<Port> getPort(final Side side, final String location) {
        //If the reference is a reference convert it to a name.
        //If the reference is a name then the conversion is a no op.
        String portName = convertPortReferenceToName(location);

        return Optional.ofNullable(portMap.get(side).get(portName));
    }

    /**
     * Get the region grids for the given side.
     *
     * @param side The side: ALLIES or AXIS.
     * @return A list of region grids for the given side.
     */
    public List<RegionGrid> getRegionGrids(final Side side) {
        return new ArrayList<>(regionRefToRegion.get(side).values());
    }

    /**
     * Get the base grids for the given side.
     *
     * @param side The side: ALLIES or AXIS.
     * @return A list of base grids for the given side.
     */
    public List<BaseGrid> getBaseGrids(final Side side) {
        return new ArrayList<>(baseRefToBase.get(side).values());
    }

    /**
     * Get the given side's regions.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of the given side's regions. This is all nations of the given side regions.
     */
    public List<Region> getSideRegions(final Side side) {
        return regions.get(side);
    }

    /**
     * Get the given side's and nation's regions.
     *
     * @param side THe side ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's regions.
     */
    public List<Region> getNationRegions(final Side side, final Nation nation) {
        return nationRegionMap.get(side).get(nation);
    }

    /**
     * Get the given side's and nation's airfields.
     *
     * @param side The side ALLIES or AXIS.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's airfields.
     */
    public List<Airfield> getNationAirfields(final Side side, final Nation nation) {
        return nationAirfieldMap.get(side).get(nation);
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
     * Add a task force to this map. If a task force grid already exists for the given task force's map reference,
     * then the task force is added to that task force grid.
     *
     * @param side The side: ALLIES or AXIS.
     * @param taskForce The task force added to this map.
     */
    public void addTaskForce(final Side side, final TaskForce taskForce) {

        taskForceGrids
                .get(side)
                .stream()
                .filter(taskForceGrid -> taskForceGrid.getReference().equalsIgnoreCase(taskForce.getReference()))
                .findFirst()
                .ifPresentOrElse(taskForceGrid -> taskForceGrid.add(taskForce),
                                 () -> createTaskForceGrid(side, taskForce));
    }

    /**
     * Determine if the given reference is a base for the given side.
     *
     * @param side The side ALLIES or AXIS.
     * @param location The map reference reference.
     * @return True if the given reference is a base for the given side.
     */
    public boolean isLocationBase(final Side side, final String location) {
        //If the reference is a name convert it to a reference.
        //If the reference is a reference then the conversion is a no op.
        String mapRef = convertNameToReference(location);

        return portRefToName.get(side).containsKey(mapRef)
                || airfieldRefToName.get(side).containsKey(mapRef);
    }


    /**
     * Determine fi the given reference is a base for either side.
     *
     * @param gameGrid A game map grid.
     * @return True if the grid corresponds to base. False otherwise.
     */
    public boolean isLocationBase(final GameGrid gameGrid) {
        String mapRef = gameGrid.getMapReference();

        for (Side side : Side.values()) {
            if (portRefToName.get(side).containsKey(mapRef)
                    || airfieldRefToName.get(side).containsKey(mapRef)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine if all of a given sides regions minimum squadron requirements are met.
     *
     * @param side The side ALLIES or AXIS.
     * @return A list of regions for which the minimum squadron requirement
     * is not satisfied.
     */
    public List<Region> areAllRegionsSatisfied(final Side side) {
        return regions
                .get(side)
                .stream().filter(region -> !region.minimumSatisfied())
                .collect(toList());
    }

    public String convertReferenceToName(final String reference) {
        String name = convertPortReferenceToName(reference);

        if (name.equalsIgnoreCase(reference)) {
            name = convertAirfieldReferenceToName(reference);
        }

        if (name.equalsIgnoreCase(ANY_ENEMY_BASE)) {
            name = "Any enemy base";
        }

        if (name.equalsIgnoreCase(ANY_FRIENDLY_BASE)) {
            name = "Any friendly base";
        }

        return name;
    }


    /**
     * Convert a reference name to a map reference. For example, the name Gibraltar is converted to H22.
     *
     * @param name A named reference on the map.
     * @return The corresponding map reference of where the name is located.
     */
    public String convertNameToReference(@NonNull final String name) {
        Matcher matcher = PATTERN.matcher(name);
        return  matcher.matches() ? name : Optional.ofNullable(getBaseReference(name)).orElse(name);
    }

    /**
     * Convert a map reference to a reference name. For example, the reference H22 is converted to Gibraltar.
     *
     * @param reference A map reference.
     * @return The corresponding port reference name.
     */
    public String convertPortReferenceToName(final String reference) {
        return Optional.ofNullable(getPortName(reference)).orElse(reference);
    }

    /**
     * Convert a map reference to a reference name. For example, the reference H22 is converted to Gibraltar.
     *
     * @param reference A map reference.
     * @return The corresponding airfield reference name.
     */
    public String convertAirfieldReferenceToName(final String reference) {
        return Optional.ofNullable(getAirfieldName(reference)).orElse(reference);
    }

    /**
     * Convert a game map reference into grid coordinates.
     *
     * @param mapReference game map reference.
     * @return The game grid corresponding to the given map reference.
     */
    public Optional<GameGrid> getGrid(@NonNull final String mapReference) {
        return Optional.of(gridRefMap.get(mapReference));
    }

    /**
     * Get the game grid given a row and column.
     *
     * @param row The game grid's row.
     * @param col The game grid's column.
     * @return The game grid corresponding to the given row and column.
     */
    public GameGrid getGrid(final int row, final int col) {
        return gridMap.get(row, col);
    }

    /**
     * Determine the distance between two different map references.
     *
     * @param mapReferenceOne a given map reference
     * @param mapReferenceTwo a given map reference
     * @return The distance in grids between the given two map references.
     */
    public int determineDistance(final String mapReferenceOne, final String mapReferenceTwo) {
        Optional<GameGrid> gridOne = getGrid(mapReferenceOne);
        Optional<GameGrid> gridTwo = getGrid(mapReferenceTwo);

        int rowOne = gridOne.map(GameGrid::getRow).orElse(0);
        int rowTwo = gridTwo.map(GameGrid::getRow).orElse(0);

        int columnOne = gridOne.map(GameGrid::getColumn).orElse(0);
        int columnTwo = gridTwo.map(GameGrid::getColumn).orElse(0);

        // a^2 + b^2 <= c^2, where a, b and c are the sides of the right triangle.
        int a = Math.abs(rowOne - rowTwo);
        int b = Math.abs(columnOne - columnTwo);

        return (int) Math.sqrt((a * a) + (b * b));
    }

    /**
     * Determine if the target map reference is in range of the entity starting at the starting map
     * reference given the entity's range.
     *
     * @param startingReference The entity's starting map reference.
     * @param targetReference The target's map reference.
     * @param range The range of the entity.
     * @return True if the entity is in range of the target. False otherwise.
     */
    public boolean inRange(final String startingReference, final String targetReference, final int range) {

        Optional<GameGrid> targetGrid = getGrid(targetReference);
        Optional<GameGrid> startingGrid = getGrid(startingReference);

        int targetRow = targetGrid.map(GameGrid::getRow).orElse(0);
        int targetColumn = targetGrid.map(GameGrid::getColumn).orElse(0);

        int startingRow = startingGrid.map(GameGrid::getRow).orElse(0);
        int startingColumn = startingGrid.map(GameGrid::getColumn).orElse(0);

        // a^2 + b^2 <= c^2, where a, b and c are the sides of the right triangle.
        int a = Math.abs(targetRow - startingRow);
        int b = Math.abs(targetColumn - startingColumn);
        int c = range + 1;

        log.debug("a: {} ,b: {}, c: {}", new Object[]{a, b, c});

        return (a * a) + (b * b) <= (c * c);
    }

    /**
     * Build the map grids.
     *
     * @param props Map properties.
     */
    private void buildGrid(final MapProps props) {
        int currentNumberOfRows = rows;

        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < currentNumberOfRows; row++) {
                GameGrid gameGrid = new GameGrid(row, col);
                String mapReference = gameGrid.getMapReference();

                GridType gridType = GridType.valueOf(props.getString(mapReference, defaultGridType));
                gameGrid.setType(gridType);

                gridMap.put(row, col, gameGrid);
                gridRefMap.put(mapReference, gameGrid);
            }

            currentNumberOfRows = (currentNumberOfRows == rows) ? rows - 1 : rows;
        }
    }

    /**
     * Build a reference map reference to base name map for the given side. Both ports and airfields are
     * included in this map.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildLocationToBaseMap(final Side side) {
        Map<String, String> portRefToNameMap = getPorts(side)
                .stream()
                .collect(Collectors.toMap(Port::getReference,
                                          Port::getName,
                                          (oldValue, newValue) -> newValue));

        Map<String, String> airfieldRefToNameMap = getAirfields(side)
                .stream()
                .collect(Collectors.toMap(Airfield::getReference,
                                          Airfield::getName,
                                          (oldValue, newValue) -> newValue));

        Map<String, String> nameToRefMap = getBases(side)
                .distinct()
                .collect(Collectors.toMap(Base::getName,
                                          Base::getReference,
                                          (oldValue, newValue) -> newValue));

        buildBaseRefToBaseMap(side);

        portRefToName.put(side, portRefToNameMap);
        airfieldRefToName.put(side, airfieldRefToNameMap);
        baseNameToRef.put(side, nameToRefMap);
    }

    /**
     * Build a map of region central map references to region grids.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildRegionRefToRegionMap(final Side side) {
        Map<String, RegionGrid> regionRefToRegionGrid = regions
                .get(side)
                .stream()
                .filter(region -> region.getMapRef() != null)
                .collect(Collectors.toMap(Region::getMapRef,
                                          this::getRegionGrid,
                                          RegionGrid::addNation));

        regionRefToRegion.put(side, regionRefToRegionGrid);
    }

    /**
     * Build a map of base map references to base grids.
     *
     * @param side The side ALLIES or AXIS.
     */
    private void buildBaseRefToBaseMap(final Side side) {
        Map<String, BaseGrid> baseRefToBaseGrid = getPorts(side)
                .stream()
                .map(this::getPortBaseGrid)
                .collect(Collectors.toMap(BaseGrid::getReference,
                                          baseGrid -> baseGrid));

        Map<Boolean, List<Airfield>> airbases = getAirfields(side)
                .stream()
                .collect(Collectors.partitioningBy(airfield -> baseRefToBaseGrid.containsKey(airfield.getReference())));

        airbases.get(true).forEach(airfield -> baseRefToBaseGrid.get(airfield.getReference()).setAirfield(airfield));
        airbases.get(false).forEach(airfield -> {
            BaseGrid baseGrid = getAirfieldBaseGrid(airfield);
            baseRefToBaseGrid.put(airfield.getReference(), baseGrid);
        });

        baseRefToBase.put(side, baseRefToBaseGrid);
    }

    /**
     * Get a region grid.
     *
     * @param region The region.
     * @return The corresponding region grid for the given region.
     */
    private RegionGrid getRegionGrid(final Region region) {
        return regionGridProvider
                .get()
                .init(region);
    }

    /**
     * Get the port base grid.
     *
     * @param port The port.
     * @return The base grid for the given port.
     */
    private BaseGrid getPortBaseGrid(final Port port) {
        return baseGridProvider
                .get()
                .initPort(port);
    }

    /**
     * get the airfield base grid.
     *
     * @param airfield The airfield
     * @return The airfield gird for the given airfield.
     */
    private BaseGrid getAirfieldBaseGrid(final Airfield airfield) {
        return baseGridProvider
                .get()
                .initAirfield(airfield);
    }

    /**
     * Build a map of nation to list of regions.
     *
     * @param side The side ALLIES or AXIS.
     * @return A map of nation to list of regions.
     */
    private Map<Nation, List<Region>> buildRegionMap(final Side side) {
        return regions
                .get(side)
                .stream()
                .map(this::createNationRegionPair)
                .collect(Collectors.toMap(Pair::getKey, this::createList, ListUtils::union));
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
    private String getPortName(final String reference) {
        String name = portRefToName.get(Side.ALLIES).get(reference);
        if (name == null) {
            name = portRefToName.get(Side.AXIS).get(reference);
        }

        // name may be null at this point if the reference does not map to a port.
        return name;
    }

    /**
     * Get the base name given the base reference.
     *
     * @param reference The base's map reference.
     * @return The name of the base.
     */
    private String getAirfieldName(final String reference) {
        String name = airfieldRefToName.get(Side.ALLIES).get(reference);
        if (name == null) {
            name = airfieldRefToName.get(Side.AXIS).get(reference);
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
     * @return A set of nations for the given side.
     */
    private Set<Nation> buildNationsMap(final Side side) {
        return regions.get(side)
                .stream()
                .map(Region::getNation)
                .collect(Collectors.toSet());
    }

    /**
     * Build a map of a nation to airfield list.
     *
     * @param side The side ALLIES or AXIS.
     * @return A map of nation to airfields.
     */
    private Map<Nation, List<Airfield>> buildNationAirfieldMap(final Side side) {
        Map<Nation, List<Airfield>> map = new HashMap<>();

        nations
                .get(side)
                .forEach(nation -> map.put(nation, getNationsAirfields(side, nation)));

        return map;
    }

    /**
     * Get the given nation's airfields.
     *
     * @param side The side ALLIES or AXIS that the nation is on.
     * @param nation The nation: BRITISH, ITALIAN, etc...
     * @return A list of the given nation's airfields.
     */
    private List<Airfield> getNationsAirfields(final Side side, final Nation nation) {
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

    /**
     * Create a list of nation, region pairs.
     *
     * @param region The map region.
     * @return A stream of nation, region pairs.
     */
    private Pair<Nation, Region> createNationRegionPair(final Region region) {
        return new Pair<>(region.getNation(), region);
    }

    /**
     * Create a list of map regions from a pair of nation,region.
     *
     * @param pair  A nation, region pair.
     * @return A list of regions.
     */
    private  List<Region> createList(final Pair<Nation, Region> pair) {
        Region region = pair.getValue();
        List<Region> list = new ArrayList<>();
        list.add(region);
        return list;
    }

    /**
     * Create a task force grid.
     *
     * @param side The side: ALLIES or AXIS.
     * @param taskForce The task force.
     */
    private void createTaskForceGrid(final Side side, final TaskForce taskForce) {
        TaskForceGrid grid = taskForceGridProvider.get().init(taskForce);
        taskForceGrids.get(side).add(grid);
    }
}
