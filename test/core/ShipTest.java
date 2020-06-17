package core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    @Test
    void getLength() {
        Ship ship = new Battleship();
        assertEquals(4, ship.getLength());
        ship = new EmptySea();
        assertEquals(1, ship.getLength());
    }

    @Test
    // Tests both getBowRow and setBowRow
    void getSetBowRow() {
        Ship ship = new Cruiser();
        ship.setBowRow(7);
        assertEquals(7, ship.getBowRow());
    }

    @Test
    // Tests both getBowColumn and setBowColumn
    void getSetBowColumn() {
        Ship ship = new Cruiser();
        ship.setBowColumn(7);
        assertEquals(7, ship.getBowColumn());
    }

    @Test
    // Tests both isHorizontal and setHorizontal
    void getSetHorizontal() {
        Ship ship = new Cruiser();
        ship.setHorizontal(true);
        assertTrue(ship.isHorizontal());
    }

    @Test
    void getShipType() {
        Ship ship = new Destroyer();
        assertEquals("destroyer", ship.getShipType());
    }

    @Test
    void placeShipAt() {
        Ocean ocean = new Ocean();
        Ship battleship = new Battleship();
        battleship.placeShipAt(6, 0, false, ocean);
        for (int i = 6; i < 10; ++i)
            assertSame(battleship, ocean.getShipArray()[i][0]);
        assertFalse(battleship.isHorizontal());
        assertEquals(6, battleship.getBowRow());
        assertEquals(0, battleship.getBowColumn());
    }

    @Test
    void okToPlaceShipAt() {
        Ocean ocean = new Ocean();
        Ship battleship = new Battleship();
        assertFalse(battleship.okToPlaceShipAt(7, 0, false, ocean));
        assertTrue(battleship.okToPlaceShipAt(6, 0, false, ocean));
        battleship.placeShipAt(6, 0, true, ocean);
        assertFalse(new Submarine().okToPlaceShipAt(5, 1, true, ocean));
        assertTrue(new Submarine().okToPlaceShipAt(4, 1, true, ocean));
    }

    @Test
    void shootAt() {
        Ship ship = new Destroyer();
        Ocean ocean = new Ocean();
        ship.placeShipAt(3, 3, true, ocean);
        assertFalse(ship.shootAt(4, 4));
        assertTrue(ship.shootAt(3, 4));
        assertTrue(ship.hit[1]);
    }

    @Test
    void isSunk() {
        Ship ship = new Destroyer();
        Ocean ocean = new Ocean();
        ship.placeShipAt(3, 3, true, ocean);
        ship.shootAt(3, 4);
        assertFalse(ship.isSunk());
        ship.shootAt(3, 3);
        assertTrue(ship.isSunk());
    }

    @Test
    void isDamaged() {
        Ship ship = new Destroyer();
        Ocean ocean = new Ocean();
        ship.placeShipAt(3, 3, true, ocean);
        assertFalse(ship.isDamaged());
        ship.shootAt(3, 4);
        assertTrue(ship.isDamaged());
        assertTrue(new EmptySea().isDamaged());
    }

    @Test
    void testToString() {
        assertEquals(new Submarine().toString(), "S");
        assertEquals(new EmptySea().toString(), "-");
    }
}