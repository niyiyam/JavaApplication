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
}
