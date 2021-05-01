package engima.waratsea.model.map;

import lombok.Getter;

/**
 * Represents a point on the game map.
 */
public class Point {
    @Getter private final int x;
    @Getter private final int y;

    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
}
