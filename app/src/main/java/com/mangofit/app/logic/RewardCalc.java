package com.mangofit.app.logic;

/**
 * 하루 보상 규칙. 오늘 누적 점수가 어느 구간에 있는지에 따라 이파리·경험치가 달라진다.
 * 기록 하나가 구간 경계를 넘으면 구간별로 나눠 계산한다.
 * 계산은 소수로 하고, 기록에 저장할 점수·이파리·경험치는 각각 반올림한다.
 */
public final class RewardCalc {

    public static final double BONUS_END = 0.25;
    public static final double NORMAL_END = 1.0;
    public static final double OVER_END = 1.5;

    public enum Zone { BONUS, NORMAL, OVER, MAXED }

    private RewardCalc() { }

    public static double rawScore(double met, int minutes) {
        return met * minutes;
    }

    /**
     * @param quota       오늘 고정된 할당량
     * @param scoreBefore 이 기록 전까지의 오늘 누적 점수
     * @param rawScore    이 기록의 점수(반올림 전)
     */
    public static Reward calc(int quota, int scoreBefore, double rawScore) {
        double from = scoreBefore;
        double to = scoreBefore + rawScore;
        double bonusEnd = quota * BONUS_END;
        double normalEnd = quota * NORMAL_END;
        double overEnd = quota * OVER_END;

        double leaves = 2 * overlap(from, to, 0, bonusEnd)
                + overlap(from, to, bonusEnd, normalEnd);
        double exp = overlap(from, to, 0, normalEnd)
                + 0.5 * overlap(from, to, normalEnd, overEnd);

        return new Reward((int) Math.round(rawScore), (int) Math.round(leaves), (int) Math.round(exp));
    }

    /** 하루에 받을 수 있는 최대 이파리·경험치 (할당량 × 1.25) */
    public static int dailyMax(int quota) {
        return (int) Math.round(quota * 1.25);
    }

    public static Zone zoneOf(int score, int quota) {
        if (score < quota * BONUS_END) return Zone.BONUS;
        if (score < quota * NORMAL_END) return Zone.NORMAL;
        if (score < quota * OVER_END) return Zone.OVER;
        return Zone.MAXED;
    }

    private static double overlap(double from, double to, double lo, double hi) {
        return Math.max(0, Math.min(to, hi) - Math.max(from, lo));
    }

    public static final class Reward {
        public final int score;
        public final int leaves;
        public final int exp;

        public Reward(int score, int leaves, int exp) {
            this.score = score;
            this.leaves = leaves;
            this.exp = exp;
        }
    }
}
