package core;

public abstract class Ship {
    final int MAX_SHIP_LENGTH = 4;                              // Maximum length of a ship

    private int bowRow, bowColumn;                              // Row and column of ship's bow
    protected int length;                                       // Length of ship
    private boolean horizontal;                                 // true, if ship is aligned horizontally, false, if vertically
    protected boolean[] hit = new boolean[MAX_SHIP_LENGTH];     // Represents cells of the ship that were hit by player
    protected boolean damaged = false;                          // true, if ship was shot at, false otherwise

    /**
     * Get the length of ship
     * @return ship's length
     */
    public int getLength() {
        return length;
    }

    /**
     * Get ship bow's row
     * @return ship bow's row
     */
    int getBowRow() {
        return bowRow;
    }

    /**
     * Set ship bow's row
     * @param row new value of ship bow's row
     */
    void setBowRow(int row) {
        bowRow = row;
    }

    /**
     * Get ship bow's column
     * @return ship bow's column
     */
    int getBowColumn() {
        return bowColumn;
    }

    /**
     * Set ship bow's column
     * @param column new value of ship bow's column
     */
    void setBowColumn(int column) {
        bowColumn = column;
    }

    /**
     * Check, if the ship is aligned horizontally
     * @return true, if ship is horizontal, false otherwise
     */
    public boolean isHorizontal() {
        return horizontal;
    }

    /**
     * Set the alignment of the ship
     * @param horizontal pass true to align horizontally; pass false to align vertically
     */
    void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    /**
     * Get name of the ship's type
     * @return name of the ship's type
     */
    public abstract String getShipType();

    void removeShip(Ocean ocean) {
        Ship[][] ships = ocean.getShipArray();
        if (horizontal) {
            for (int i = 0; i < length; ++i)
                ships[bowRow][bowColumn + i] = new EmptySea();
        }
        else {
            for (int i = 0; i < length; ++i)
                ships[bowRow + i][bowColumn] = new EmptySea();
        }
    }

    /**
     * Check, if placing the ship at given position won't break the rules
     * @param row row of potential place
     * @param column column of potential place
     * @param horizontal true, if ship should be placed horizontally, false otherwise
     * @param ocean Ocean object to place ship at
     * @return true, if ship can be placed according to the rules, false otherwise
     */
    boolean okToPlaceShipAt(int row, int column, boolean horizontal, Ocean ocean) {
        if (horizontal) {
            if (column + length > ocean.OCEAN_SIZE)
                return false;
            for (int i = -1; i <= length; ++i) {
                if (ocean.isOccupied(row, column + i) ||
                    ocean.isOccupied(row - 1, column + i) ||
                    ocean.isOccupied(row + 1, column + i))
                    return false;
            }
            return true;
        }
        if (row + length > ocean.OCEAN_SIZE)
            return false;
        for (int i = -1; i <= length; ++i) {
            if (ocean.isOccupied(row + i, column) ||
                ocean.isOccupied(row + i, column + 1) ||
                ocean.isOccupied(row + i, column - 1))
                return false;
        }
        return true;
    }

    /**
     * Place the ship at given position
     * @param row row of the place
     * @param column column of the place
     * @param horizontal true, if ship should be placed horizontally, false otherwise
     * @param ocean Ocean object to place ship at
     */
    void placeShipAt(int row, int column, boolean horizontal, Ocean ocean) {
        bowRow = row;
        bowColumn = column;
        this.horizontal = horizontal;
        Ship[][] ships = ocean.getShipArray();
        if (horizontal) {
            for (int i = 0; i < length; ++i)
                ships[row][column + i] = this;
        }
        else {
            for (int i = 0; i < length; ++i)
                ships[row + i][column] = this;
        }
    }

    /**
     * Shoot at given position
     * @param row row of the shot's position
     * @param column column of the shot's position
     * @return true, if shot was successful, false if not or ship has already been sunk
     */
    boolean shootAt(int row, int column) {
        if (isSunk())
            return false;
        if (horizontal) {
            if (row != bowRow)
                return false;
            int dist = column - bowColumn;
            if (dist >= length || dist < 0)
                return false;
            damaged = true;
            return hit[dist] = true;
        }
        if (column != bowColumn)
            return false;
        int dist = row - bowRow;
        if (dist > length || dist < 0)
            return false;
        damaged = true;
        return hit[dist] = true;
    }

    /**
     * Check, if ship has already been sunk
     * @return true, if ship has been sunk, false otherwise
     */
    public boolean isSunk() {
        for (int i = 0; i < length; i++)
            if (!hit[i])
                return false;
        return true;
    }

    /**
     * Check, if the ship has been damaged
     * @return value of damaged field
     */
    boolean isDamaged() {
        return damaged;
    }

    /**
     * Get the symbol for the ship to represent in output
     * @return symbol to represent the ship
     */
    @Override
    public String toString() {
        return "S";
    }
}
