package engima.waratsea.model.map.region;

import com.google.inject.Inject;
import com.google.inject.Provider;
import engima.waratsea.model.game.Nation;
import engima.waratsea.model.game.Side;
import engima.waratsea.model.map.GameGrid;
import engima.waratsea.model.map.GameMap;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class RegionGrid {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Side side;

    @Getter
    private final Set<Nation> nations = new HashSet<>();

    @Getter
    private final Set<Region> regions = new HashSet<>();

    @Getter
    @Setter
    private GameGrid gameGrid;

    private final Provider<GameMap> gameMapProvider;

    /**
     * Constructor called by guice.
     *
     * @param gameMapProvider Provides game maps.
     */
    @Inject
    public RegionGrid(final Provider<GameMap> gameMapProvider) {
        this.gameMapProvider = gameMapProvider;
    }

    /**
     * Initialize from a region.
     *
     * @param region The region that the region grid is associated with.
     * @return This region grid.
     */
    public RegionGrid init(@Nonnull final Region region) {
        name = region.getName();
        side = region.getSide();
        nations.add(region.getNation());
        regions.add(region);
        this.gameGrid = gameMapProvider
                .get()
                .getGrid(region.getMapRef())
                .orElse(null);

        return this;
    }

    /**
     * Add a nation to this region grid.
     *
     * @param regionGrid The region grid whose nation and region are added.
     * @return This region grid.
     */
    public RegionGrid addNation(@Nonnull final RegionGrid regionGrid) {
        nations.addAll(regionGrid.getNations());
        regions.addAll(regionGrid.getRegions());
        return this;
    }
}
