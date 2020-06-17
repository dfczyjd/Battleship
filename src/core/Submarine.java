package core;

public class Submarine extends Ship {
    public Submarine() {
        length = 1;
        hit[0] = false;
    }

    /**
     * Get name of the ship's type (always "submarine")
     * @return string "submarine"
     */
    @Override
    public String getShipType() {
        return "submarine";
    }
}
