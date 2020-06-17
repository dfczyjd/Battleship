package core;

class Destroyer extends Ship {
    Destroyer() {
        length = 2;
        hit[0] = false;
        hit[1] = false;
    }

    /**
     * Get name of the ship's type (always "destroyer")
     * @return string "destroyer"
     */
    @Override
    public String getShipType() {
        return "destroyer";
    }
}
