package com.mangofit.app.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 화면에 보여줄 숫자·날짜 형식 */
public final class Fmt {

    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("M/d");

    private Fmt() { }

    /** 1240 → "1,240" */
    public static String num(int n) {
        return String.format(Locale.KOREA, "%,d", n);
    }

    /** 3.5 → "3.5", 6.0 → "6" */
    public static String decimal(double d) {
        if (d == Math.rint(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    /** 2026-06-27 → "2026.06.27" */
    public static String dotDate(LocalDate date) {
        return date.format(DOT_DATE);
    }

    /** 2026-06-27 → "6/27" */
    public static String shortDate(LocalDate date) {
        return date.format(SHORT_DATE);
    }

    /** 0~1000 막대 진행도 */
    public static int permille(int current, int max) {
        if (max <= 0) return 0;
        return (int) Math.min(1000, Math.round(current * 1000.0 / max));
    }

    /** 초 → "12:05" 또는 "1:02:05" (타이머 표시) */
    public static String clock(int seconds) {
        int h = seconds / 3600, m = seconds % 3600 / 60, s = seconds % 60;
        if (h > 0) return String.format(Locale.KOREA, "%d:%02d:%02d", h, m, s);
        return String.format(Locale.KOREA, "%02d:%02d", m, s);
    }

    /** 초 → "1시간 5분 3초", "12분 30초", "45초" (0인 단위는 뺀다) */
    public static String duration(int seconds) {
        int h = seconds / 3600, m = seconds % 3600 / 60, s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("시간");
        if (m > 0) sb.append(sb.length() > 0 ? " " : "").append(m).append("분");
        if (s > 0 || sb.length() == 0) sb.append(sb.length() > 0 ? " " : "").append(s).append("초");
        return sb.toString();
    }

    /** 한 시간 미만은 초까지 ("12분 30초"), 그 이상은 시간과 분 ("2시간 5분") */
    public static String time(int seconds) {
        return seconds < 3600 ? duration(seconds) : hoursMinutes(seconds);
    }

    /** 분 단위 큰 합계용: "12시간 30분", "45분" (초는 버림) */
    public static String hoursMinutes(int seconds) {
        int h = seconds / 3600, m = seconds % 3600 / 60;
        if (h == 0) return m + "분";
        return m == 0 ? h + "시간" : h + "시간 " + m + "분";
    }
}
