package com.mangofit.app.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PlacementTest {

    private static final float EPS = 0.001f;

    @Test
    public void origin_isMangoCenter() {
        // 망고 영역 (10, 20)에서 300×400, 아이템은 망고 너비의 50%, 정사각형
        float[] r = new Placement(0f, 0f, 0.5f).rectIn(10, 20, 300, 400, 1f);
        assertEquals(85f, r[0], EPS);   // 중앙 160 - 75
        assertEquals(145f, r[1], EPS);  // 중앙 220 - 75
        assertEquals(150f, r[2], EPS);
        assertEquals(150f, r[3], EPS);
    }

    @Test
    public void offsets_useMangoWidthAsUnit_forBothAxes() {
        // y도 망고 너비 기준: y = -0.25 → 300 × 0.25 = 75 위로
        float[] r = new Placement(0.1f, -0.25f, 0.2f).rectIn(0, 0, 300, 400, 2f);
        float w = 60f;
        float h = 30f;
        assertEquals(w, r[2], EPS);
        assertEquals(h, r[3], EPS);
        assertEquals(150f + 30f - w / 2f, r[0], EPS);
        assertEquals(200f - 75f - h / 2f, r[1], EPS);
    }

    @Test
    public void scalesWithMangoSize() {
        Placement p = new Placement(0.2f, 0.3f, 0.4f);
        float[] small = p.rectIn(0, 0, 100, 140, 1.5f);
        float[] big = p.rectIn(0, 0, 200, 280, 1.5f);
        for (int i = 0; i < 4; i++) assertEquals(small[i] * 2, big[i], EPS);
    }

    @Test
    public void everyCatalogItem_hasPlacement() {
        for (Item item : ItemCatalog.all()) assertNotNull(item.id, item.placement);
    }
}
