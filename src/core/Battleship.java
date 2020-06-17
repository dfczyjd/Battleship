package core;

class Battleship extends Ship {
    Battleship() {
        length = 4;
        hit[0] = false;
        hit[1] = false;
        hit[2] = false;
        hit[3] = false;
    }

    /**
     * Get name of the ship's type (always "battleship")
     * @return string "battleship"
     */
    @Override
    public String getShipType() {
        return "battleship";
    }
}
