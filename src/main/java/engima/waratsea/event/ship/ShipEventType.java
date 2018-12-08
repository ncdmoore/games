package engima.waratsea.event.ship;

/**
 * Represents ship event types. This is needed because ship event types may contain wild cards such as "ANY",
 * which represents any ship type.
 */
public enum ShipEventType {
    ANY,
    AIRCRAFT_CARRIER,
    BATTLESHIP,
    CRUISER,
    DESTROYER,
    DESTROYER_ESCORT,
    OTHER
}
