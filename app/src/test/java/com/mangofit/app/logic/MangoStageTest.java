package com.mangofit.app.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MangoStageTest {
    @Test public void growthChangesAtLevelBoundaries() {
        MangoStage[] expected = {MangoStage.GREEN, MangoStage.YELLOW_GREEN, MangoStage.YELLOW,
                MangoStage.GOLDEN_YELLOW, MangoStage.ORANGE};
        for (int i = 0; i < expected.length; i++) {
            int threshold = Levels.cumulativeExp(1 + i * 2);
            assertEquals(expected[i], MangoStage.forLevel(Levels.levelForExp(threshold)));
            if (i > 0) {
                assertEquals(expected[i - 1], MangoStage.forLevel(Levels.levelForExp(threshold - 1)));
            }
        }
        assertEquals(MangoStage.ORANGE, MangoStage.forLevel(20));
    }
}
