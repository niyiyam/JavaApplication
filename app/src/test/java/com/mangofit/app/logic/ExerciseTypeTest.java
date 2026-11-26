package com.mangofit.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ExerciseTypeTest {

    private static ExerciseType find(String name) {
        for (ExerciseType type : ExerciseType.selectable()) {
            if (type.name.equals(name)) return type;
        }
        throw new AssertionError(name + " 없음");
    }

    @Test
    public void catalogHasAllSportsAndLegacyCodes() {
        assertEquals(147, ExerciseType.selectable().size());
        assertEquals(9, ExerciseType.categories().size());
        assertNotNull(ExerciseType.fromCode("run"));
        assertFalse(ExerciseType.fromCode("run").selectable);
    }

    @Test
    public void koreanSearchMatchesWholeAndPartialSyllables() {
        ExerciseType badminton = find("배드민턴");
        assertTrue(badminton.matches("배드"));
        assertTrue(badminton.matches("드민"));
        assertTrue(badminton.matches("민 턴"));
        assertFalse(badminton.matches("축구"));
    }

    @Test
    public void koreanSearchMatchesInitialConsonantsWhileTyping() {
        ExerciseType badminton = find("배드민턴");
        assertTrue(badminton.matches("ㅂ"));
        assertTrue(badminton.matches("ㅂㄷ"));
        assertTrue(badminton.matches("배ㄷ"));
        assertTrue(badminton.matches("ㅁㅌ"));
        assertFalse(badminton.matches("ㄱ"));
    }

    @Test
    public void searchIncludesIntensityAndIgnoresCase() {
        ExerciseType bmx = find("BMX");
        assertTrue(bmx.matches("bmx"));
        boolean strongFound = false;
        for (ExerciseType type : ExerciseType.selectable()) {
            if (type.intensity.equals("강") && type.matches("강")) strongFound = true;
        }
        assertTrue(strongFound);
    }
}
