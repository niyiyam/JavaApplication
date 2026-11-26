package com.mangofit.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;

public class MangoCycleTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);

    @Test
    public void dayNumber_startIsDayOne() {
        assertEquals(1, MangoCycle.dayNumber(START, START));
        assertEquals(28, MangoCycle.dayNumber(START, LocalDate.of(2026, 1, 28)));
    }

    @Test
    public void endDate_is28thDay() {
        assertEquals(LocalDate.of(2026, 1, 28), MangoCycle.endDate(START));
    }

    @Test
    public void isOver_onlyAfter28thDay() {
        assertFalse(MangoCycle.isOver(START, LocalDate.of(2026, 1, 28)));
        assertTrue(MangoCycle.isOver(START, LocalDate.of(2026, 1, 29)));
        assertTrue(MangoCycle.isOver(START, LocalDate.of(2026, 5, 1)));
    }
}
