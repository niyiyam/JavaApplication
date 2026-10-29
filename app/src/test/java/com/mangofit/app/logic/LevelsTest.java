package com.mangofit.app.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LevelsTest {

    @Test
    public void quota_followsUserLevel() {
        assertEquals(200, Levels.quotaFor(1));
        assertEquals(250, Levels.quotaFor(2));
        assertEquals(300, Levels.quotaFor(3));
    }

    @Test
    public void expToNext_isQuotaTimesTwo() {
        assertEquals(400, Levels.expToNext(1));
        assertEquals(500, Levels.expToNext(2));
        assertEquals(1200, Levels.expToNext(9));
    }

    @Test
    public void cumulativeExp_matchesSumOfSteps() {
        assertEquals(0, Levels.cumulativeExp(1));
        assertEquals(400, Levels.cumulativeExp(2));
        assertEquals(900, Levels.cumulativeExp(3));
        int sum = 0;
        for (int level = 1; level < 10; level++) sum += Levels.expToNext(level);
        assertEquals(sum, Levels.cumulativeExp(10));
    }

    @Test
    public void progressFor_splitsLevelAndRemainder() {
        Levels.Progress p = Levels.progressFor(0);
        assertEquals(1, p.level);
        assertEquals(0, p.current);
        assertEquals(400, p.needed);

        p = Levels.progressFor(399);
        assertEquals(1, p.level);

        p = Levels.progressFor(400);
        assertEquals(2, p.level);
        assertEquals(0, p.current);

        p = Levels.progressFor(1000);
        assertEquals(3, p.level);
        assertEquals(100, p.current);
        assertEquals(600, p.needed);
    }
}
