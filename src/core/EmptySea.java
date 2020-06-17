package core;

class EmptySea extends Ship {
    EmptySea() {
        length = 1;
        damaged = true;     // Empty sea is always "damaged" so it will never enter this state again
    }

    /**
     * Get name of the ship's type (always "empty sea")
     * @return string "empty sea"
     */
    @Override
    public String getShipType() {
        return "empty sea";
    }

    /**
     * Called, if the player shoots at empty cell
     * @param row row of the shot's position
     * @param column column of the shot's position
     * @return always false
     */
    @Override
    boolean shootAt(int row, int column) {
        return false;
    }

    /**
     * Check, if the ship is sunk (always false)
     * @return always false
     */
    @Override
    public boolean isSunk() {
        return false;
    }

    /**
     * Get the symbol to represent the ship
     * @return string "-"
     */
    @Override
    public String toString() {
        return "-";
    }
}
