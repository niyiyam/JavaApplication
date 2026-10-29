package com.mangofit.app.logic;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** 망고 28일 주기. 시작일이 1일차이고 28일차가 마지막 날이다. */
public final class MangoCycle {

    public static final int DAYS = 28;

    private MangoCycle() { }

    /** 시작일을 1일차로 센 오늘의 일차 */
    public static int dayNumber(LocalDate start, LocalDate today) {
        return (int) ChronoUnit.DAYS.between(start, today) + 1;
    }

    /** 28일차 날짜 */
    public static LocalDate endDate(LocalDate start) {
        return start.plusDays(DAYS - 1);
    }

    /** 28일차가 끝났으면 true. 이 경우 앱을 열 때 정산한다. */
    public static boolean isOver(LocalDate start, LocalDate today) {
        return today.isAfter(endDate(start));
    }

    /** 시작일과 종료일을 모두 포함한 기간 일수 */
    public static int lengthDays(LocalDate start, LocalDate end) {
        return dayNumber(start, end);
    }
}
