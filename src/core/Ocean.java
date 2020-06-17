package core;

import java.util.ArrayList;
import java.util.Random;

public class Ocean {
    public final int OCEAN_SIZE = 10;      // Size of battlefield along one axis
    public final int FLEET_SIZE = 10;      // Size of the fleet

    /**
     * Status of a battlefield's cell
     */
    public enum CellStatus {
        Unknown(0),    // Player hasn't shot at the cell
        Missed(1),     // The shot at the cell missed
        Damaged(2),    // The ship at the cell is partially damaged
        DestroyedHor(3),   // The ship at the cell is destroyed
        DestroyedVer(4),
        Duplicate(5);

        private final int value;

        CellStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public boolean isDestroyed() {
            return value == 3 || value == 4;
        }
    }

    /**
     * The position of a ship's bow
     */
    static class Position {
        int x;                  // row
        int y;                  // column
        boolean horizontal;     // whether ship is aligned horizontally or not

        Position(int x, int y, boolean horizontal) {
            this.x = x;
            this.y = y;
            this.horizontal = horizontal;
        }
    }

    private Random rnd;             // Random instance
    private Ship[][] ships;         // Represents the battlefield
    private boolean[][] shotAt;     // Contains true if cell has been shot at, or false otherwise
    private int shipsSunk;          // Number of ships sunk by player
    private int[] shipsLeft;        // Numbers of ships left to place, by length

    /**
     * Constructor, initialize variables before new game
     */
    public Ocean() {
        ships = new Ship[OCEAN_SIZE][OCEAN_SIZE];
        shotAt = new boolean[OCEAN_SIZE][OCEAN_SIZE];
        for (int i = 0; i < OCEAN_SIZE; ++i) {
            for (int j = 0; j < OCEAN_SIZE; ++j) {
                ships[i][j] = new EmptySea();
                shotAt[i][j] = false;
            }
        }
        shipsSunk = 0;
        rnd = new Random();
        shipsLeft = new int[]{4, 3, 2, 1};
    }

    public Ship removeShipFrom(int row, int column) {
        Ship toRemove = ships[row][column];
        toRemove.removeShip(this);
        ++shipsLeft[toRemove.getLength() - 1];
        return toRemove;
    }

    public boolean tryPlaceShipAt(int row, int column, boolean isHorizontal, int size) {
        Ship toPlace = null;
        switch (size) {
            case 1:
                toPlace = new Submarine();
                break;

            case 2:
                toPlace = new Destroyer();
                break;

            case 3:
                toPlace = new Cruiser();
                break;

            case 4:
                toPlace = new Battleship();
        }
        if (shipsLeft[size - 1] > 0 && toPlace.okToPlaceShipAt(row, column, isHorizontal, this)) {
            toPlace.placeShipAt(row, column, isHorizontal, this);
            --shipsLeft[size - 1];
        }
        for (int val : shipsLeft) {
            if (val != 0)
                return false;
        }
        return true;
    }

    /**
     * Randomly place given ship on the battlefield
     * @param ship ship to place
     */
    private void placeOneShipRandomly(Ship ship) {
        ArrayList<Position> available = new ArrayList<>();
        for (int i = 0; i < OCEAN_SIZE; ++i) {
            for (int j = 0; j < OCEAN_SIZE; ++j) {
                if (ship.okToPlaceShipAt(i, j, true, this))
                    available.add(new Position(i, j, true));
                if (ship.okToPlaceShipAt(i, j, false, this))
                    available.add(new Position(i, j, false));
            }
        }
        int randInd = rnd.nextInt(available.size());
        Position pos = available.get(randInd);
        ship.placeShipAt(pos.x, pos.y, pos.horizontal, this);
    }

    /**
     * Randomly place all 10 ships on the battlefield
     */
    public void placeAllShipsRandomly() {
        placeOneShipRandomly(new Battleship());

        placeOneShipRandomly(new Cruiser());
        placeOneShipRandomly(new Cruiser());

        placeOneShipRandomly(new Destroyer());
        placeOneShipRandomly(new Destroyer());
        placeOneShipRandomly(new Destroyer());

        placeOneShipRandomly(new Submarine());
        placeOneShipRandomly(new Submarine());
        placeOneShipRandomly(new Submarine());
        placeOneShipRandomly(new Submarine());
    }

    /**
     * Check, whether the given cell is occupied with a ship. If the cell is outside the battlefield, returns false
     * @param row       cell's row
     * @param column    cell's column
     * @return true, if cell is occupied with a ship, false otherwise
     */
    boolean isOccupied(int row, int column) {
        if (row < 0 || column < 0 || row >= OCEAN_SIZE || column >= OCEAN_SIZE)
            return false;
        return ships[row][column].getClass() != EmptySea.class;
    }

    /**
     * Make a shot at the given cell
     * @param row       cell's row
     * @param column    cell's column
     * @return true, if shot hit any ship, false otherwise
     */
    public boolean shootAt(int row, int column) {
        Ship ship = ships[row][column];
        boolean shot = ship.shootAt(row, column);
        shotAt[row][column] = true;
        if (shot && ship.isSunk()) {
            ++shipsSunk;
        }
        return shot;
    }

    public boolean hasShipAt(int row, int column) {
        return !(ships[row][column] instanceof EmptySea);
    }

    public boolean isHorizontalAt(int row, int column) {
        return ships[row][column].isHorizontal();
    }

    /**
     * Check, if the game is over
     * @return true if player have sunk all 10 ships, false otherwise
     */
    public boolean isGameOver() {
        return shipsSunk == FLEET_SIZE;
    }

    /**
     * Get the array representing the battlefield
     * @return array representing the battlefield
     */
    public Ship[][] getShipArray() {
        return ships;
    }

    /**
     * Print the battlefield to the console for debug purposes
     */
    void print() {
        System.out.print("  ");
        for (int i = 0; i < OCEAN_SIZE; ++i)
            System.out.print(i + " ");
        System.out.println();
        for (int i = 0; i < OCEAN_SIZE; ++i) {
            System.out.print(i + " ");
            for (int j = 0; j < OCEAN_SIZE; ++j) {
                System.out.print(ships[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Get status of the cell
     * @param row cell's row
     * @param column cell's column
     * @return status of the cell
     */
    public CellStatus getCellStatus(int row, int column) {
        if (row < 0 || column < 0 || row >= OCEAN_SIZE || column >= OCEAN_SIZE)
            return CellStatus.Unknown;
        if (!shotAt[row][column])
            return CellStatus.Unknown;
        if (isOccupied(row, column)) {
            if (ships[row][column].isSunk())
                return ships[row][column].isHorizontal() ? CellStatus.DestroyedHor : CellStatus.DestroyedVer;
            return CellStatus.Damaged;
        }
        return CellStatus.Missed;
    }

    /**
     * Check, if the player has already shot at cell
     * @param row cell's row
     * @param column cell's column
     * @return true, if player has shot, false otherwise
     */
    public boolean hasShotAt(int row, int column) {
        return shotAt[row][column];
    }

    /**
     * Sets seed for Random's instance for testing purposes
     * @param seed seed to set
     */
    void setRndSeed(long seed) {
        rnd.setSeed(seed);
    }
}
