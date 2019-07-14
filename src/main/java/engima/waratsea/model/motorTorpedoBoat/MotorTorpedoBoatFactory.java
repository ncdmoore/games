package engima.waratsea.model.motorTorpedoBoat;

import engima.waratsea.model.motorTorpedoBoat.data.MotorTorpedoBoatData;

/**
 * Creates submarines.
 */
public interface MotorTorpedoBoatFactory {
    /**
     * Creates a motor torpedo boat.
     *
     * @param data The motor torpedo boat's data.
     * @return The motor torpedo boat.
     */
    MotorTorpedoBoat create(MotorTorpedoBoatData data);
}
