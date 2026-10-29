package com.mangofit.app.logic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RewardCalcTest {

    // 기획서 예시: 할당량 600, 오늘 0점에서 걷기 30분 → 요가 30분
    @Test
    public void specExample_walkThenYoga() {
        RewardCalc.Reward walk = RewardCalc.calc(600, 0, RewardCalc.rawScore(3.5, 30));
        assertEquals(105, walk.score);
        assertEquals(210, walk.leaves);
        assertEquals(105, walk.exp);

        RewardCalc.Reward yoga = RewardCalc.calc(600, 105, RewardCalc.rawScore(2.5, 30));
        assertEquals(75, yoga.score);
        assertEquals(120, yoga.leaves);
        assertEquals(75, yoga.exp);
    }

    @Test
    public void overQuota_noLeaves_halfExp() {
        RewardCalc.Reward r = RewardCalc.calc(600, 600, 100);
        assertEquals(0, r.leaves);
        assertEquals(50, r.exp);
    }

    @Test
    public void beyond150Percent_nothing() {
        RewardCalc.Reward r = RewardCalc.calc(600, 900, 100);
        assertEquals(100, r.score);
        assertEquals(0, r.leaves);
        assertEquals(0, r.exp);
    }

    @Test
    public void dailyMax_reachedAt100And150Percent() {
        RewardCalc.Reward toFull = RewardCalc.calc(600, 0, 600);
        assertEquals(750, toFull.leaves);
        assertEquals(600, toFull.exp);

        RewardCalc.Reward all = RewardCalc.calc(600, 0, 2000);
        assertEquals(750, all.leaves);
        assertEquals(750, all.exp);
        assertEquals(750, RewardCalc.dailyMax(600));
    }

    @Test
    public void decimals_roundedPerRecord() {
        // 걷기 7분 = 24.5점 → 25
        RewardCalc.Reward r = RewardCalc.calc(250, 0, RewardCalc.rawScore(3.5, 7));
        assertEquals(25, r.score);
        assertEquals(49, r.leaves);
        assertEquals(25, r.exp);
    }

    @Test
    public void zoneOf_boundaries() {
        assertEquals(RewardCalc.Zone.BONUS, RewardCalc.zoneOf(0, 600));
        assertEquals(RewardCalc.Zone.NORMAL, RewardCalc.zoneOf(150, 600));
        assertEquals(RewardCalc.Zone.OVER, RewardCalc.zoneOf(600, 600));
        assertEquals(RewardCalc.Zone.MAXED, RewardCalc.zoneOf(900, 600));
    }
}
