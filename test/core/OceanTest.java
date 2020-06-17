package core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OceanTest {
    private Ocean ocean;

    @BeforeEach
    void setUp() {
        ocean = new Ocean();
        ocean.setRndSeed(123009);
    }

    @Test
    void placeAllShipsRandomly() {
    }

    @Test
    void isOccupied() {
        assertFalse(ocean.isOccupied(10, 7));
        Ship ship = new Submarine();
        assertFalse(ocean.isOccupied(3, 3));
        ship.placeShipAt(3, 3, true, ocean);
        assertTrue(ocean.isOccupied(3, 3));
    }

    @Test
    // Tests shootAt(), getNotFoundShips() and getters for shotsFired, shipsDamaged and shipsSunk
    // TODO: edit checker
    void shootAt() {
        Ship ship = new Destroyer();
        ship.placeShipAt(2, 1, true, ocean);
        ocean.shootAt(2, 0);
        ocean.shootAt(2, 1);
        ocean.shootAt(2, 2);
    }

    @Test
    void isGameOver() {
        ocean.placeAllShipsRandomly();
        for (int i = 0; i < ocean.OCEAN_SIZE; ++i)
            for (int j = 0; j < ocean.OCEAN_SIZE; ++j)
                ocean.shootAt(i, j);
        assertTrue(ocean.isGameOver());
    }

    @Test
    void getCellStatus() {
        Ship ship = new Destroyer();
        ship.placeShipAt(2, 2, true, ocean);
        ocean.shootAt(2, 2);
        ocean.shootAt(3, 2);
        assertEquals(Ocean.CellStatus.Unknown, ocean.getCellStatus(10, 2));
        assertEquals(Ocean.CellStatus.Unknown, ocean.getCellStatus(1, 2));
        assertEquals(Ocean.CellStatus.Missed, ocean.getCellStatus(3, 2));
        assertEquals(Ocean.CellStatus.Damaged, ocean.getCellStatus(2, 2));
        ocean.shootAt(2, 3);
        assertEquals(Ocean.CellStatus.DestroyedHor, ocean.getCellStatus(2, 2));
    }

    @Test
    void hasShotAt() {
        assertFalse(ocean.hasShotAt(0, 0));
        ocean.shootAt(0, 0);
        assertTrue(ocean.hasShotAt(0, 0));
    }
}