package core;

class Cruiser extends Ship {
    Cruiser() {
        length = 3;
        hit[0] = false;
        hit[1] = false;
        hit[2] = false;
    }

    /**
     * Get name of the ship's type (always "cruiser")
     * @return string "cruiser"
     */
    @Override
    public String getShipType() {
        return "cruiser";
    }
}
