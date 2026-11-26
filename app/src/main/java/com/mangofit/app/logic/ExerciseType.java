package com.mangofit.app.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 운동 종목과 MET. 점수 = MET × 시간(분, 초는 소수로).
 * 수치 출처: 2024 Adult Compendium of Physical Activities (19~59세 성인).
 * 강도가 4단계 이상인 종목은 약/강 두 항목으로 나눈다. estimated = 실측이 아닌 추정값.
 * 예전 버전의 7종 코드(walk 등)는 기존 기록을 보여주기 위해 남기고, 고르는 목록에서는 뺀다.
 */
public final class ExerciseType {

    public final String code;
    public final String category;
    public final String name;
    /** "약", "강" 또는 빈 문자열 */
    public final String intensity;
    /** 강도 설명 (예: 시속 약 9km). 없으면 빈 문자열 */
    public final String note;
    public final double met;
    public final boolean estimated;
    /** 고르는 목록에 보이는지 (예전 코드는 false) */
    public final boolean selectable;

    private ExerciseType(String code, String category, String name, String intensity, String note,
                         double met, boolean estimated, boolean selectable) {
        this.code = code;
        this.category = category;
        this.name = name;
        this.intensity = intensity;
        this.note = note;
        this.met = met;
        this.estimated = estimated;
        this.selectable = selectable;
    }

    /** 목록과 기록에 보이는 이름. 예: "달리기 (러닝머신 포함) - 강" */
    public String label() {
        return intensity.isEmpty() ? name : name + " - " + intensity;
    }

    @Override
    public String toString() {
        return label();
    }

    private static final Map<String, ExerciseType> BY_CODE = new LinkedHashMap<>();
    private static final List<ExerciseType> SELECTABLE = new ArrayList<>();
    private static final List<String> CATEGORIES = new ArrayList<>();

    private static void add(String code, String category, String name, String intensity, String note,
                            double met, boolean estimated) {
        ExerciseType type = new ExerciseType(code, category, name, intensity, note, met, estimated, true);
        BY_CODE.put(code, type);
        SELECTABLE.add(type);
        if (!CATEGORIES.contains(category)) CATEGORIES.add(category);
    }

    private static void legacy(String code, String category, String name, double met) {
        BY_CODE.put(code, new ExerciseType(code, category, name, "", "", met, false, false));
    }

    static {
        add("bike_01_l", "자전거", "자전거 (실내, 전기자전거 포함)", "약", "시속 약 9km", 3.5, false);
        add("bike_01_h", "자전거", "자전거 (실내, 전기자전거 포함)", "강", "시속 32km 이상 경주", 16.8, false);
        add("bike_02", "자전거", "산악자전거(MTB)", "", "", 8.5, true);
        add("bike_03", "자전거", "BMX", "", "", 8.5, true);
        add("bike_04", "자전거", "스피닝", "", "", 9.0, false);
        add("bike_05", "자전거", "외발자전거", "", "", 5.0, true);
        add("fit_01", "헬스, 피트니스", "헬스 (헬스장 운동 일반)", "", "", 5.5, false);
        add("fit_02", "헬스, 피트니스", "홈트 (집에서 하는 운동 일반)", "", "", 3.8, false);
        add("fit_03", "헬스, 피트니스", "웨이트 트레이닝", "", "", 3.5, false);
        add("fit_04", "헬스, 피트니스", "맨몸운동", "", "", 3.8, false);
        add("fit_05", "헬스, 피트니스", "서킷 트레이닝", "", "", 5.0, false);
        add("fit_06", "헬스, 피트니스", "HIIT", "", "", 7.0, false);
        add("fit_07_l", "헬스, 피트니스", "에어로빅 (스텝 포함)", "약", "저충격", 4.8, false);
        add("fit_07_h", "헬스, 피트니스", "에어로빅 (스텝 포함)", "강", "중량 착용", 10.0, false);
        add("fit_08", "헬스, 피트니스", "일립티컬", "", "", 5.0, false);
        add("fit_09_l", "헬스, 피트니스", "조정 (로잉머신 포함)", "약", "100W 미만", 5.0, false);
        add("fit_09_h", "헬스, 피트니스", "조정 (로잉머신 포함)", "강", "200W 이상", 14.0, false);
        add("fit_10", "헬스, 피트니스", "요가", "", "", 2.3, false);
        add("fit_11", "헬스, 피트니스", "필라테스", "", "", 2.8, false);
        add("fit_12", "헬스, 피트니스", "스트레칭", "", "", 2.3, false);
        add("fit_13", "헬스, 피트니스", "짐볼 운동", "", "", 2.8, false);
        add("fit_14", "헬스, 피트니스", "훌라후프", "", "", 5.8, false);
        add("fit_15", "헬스, 피트니스", "줌바", "", "", 6.5, false);
        add("fit_16", "헬스, 피트니스", "폴댄스", "", "", 4.5, false);
        add("fit_17_l", "헬스, 피트니스", "VR, 체감형 피트니스 게임", "약", "전신 체감형", 4.0, false);
        add("fit_17_h", "헬스, 피트니스", "VR, 체감형 피트니스 게임", "강", "고강도 VR", 13.0, false);
        add("dance_01", "춤", "발레, 현대무용", "", "", 5.0, false);
        add("dance_02", "춤", "재즈댄스", "", "", 4.5, false);
        add("dance_03", "춤", "탭댄스", "", "", 4.8, false);
        add("dance_04", "춤", "살사", "", "", 4.8, false);
        add("dance_05_l", "춤", "댄스스포츠(볼룸)", "약", "느린 볼룸", 3.0, false);
        add("dance_05_h", "춤", "댄스스포츠(볼룸)", "강", "경기", 13.0, false);
        add("dance_06", "춤", "플라멩코", "", "", 8.5, false);
        add("dance_07", "춤", "일반 댄스(클럽, 라인댄스)", "", "", 3.8, false);
        add("dance_08", "춤", "민속, 전통춤", "", "", 5.0, false);
        add("runwalk_01_l", "달리기, 걷기", "달리기 (러닝머신 포함)", "약", "시속 4~6km 느린 조깅", 3.3, false);
        add("runwalk_01_h", "달리기, 걷기", "달리기 (러닝머신 포함)", "강", "시속 22.5km", 23.0, false);
        add("runwalk_02", "달리기, 걷기", "마라톤", "", "", 13.3, false);
        add("runwalk_03_l", "달리기, 걷기", "걷기 (러닝머신 포함)", "약", "시속 3km 미만", 2.3, false);
        add("runwalk_03_h", "달리기, 걷기", "걷기 (러닝머신 포함)", "강", "시속 8~9km", 8.5, false);
        add("runwalk_04", "달리기, 걷기", "노르딕 워킹", "", "", 5.3, false);
        add("runwalk_05_l", "달리기, 걷기", "경보", "약", "", 6.5, true);
        add("runwalk_05_h", "달리기, 걷기", "경보", "강", "경기 속도", 15.5, false);
        add("runwalk_06_l", "달리기, 걷기", "등산, 하이킹", "약", "느린 하이킹", 3.8, false);
        add("runwalk_06_h", "달리기, 걷기", "등산, 하이킹", "강", "경사 10~40%, 빠르게", 16.3, false);
        add("runwalk_07", "달리기, 걷기", "백패킹", "", "", 7.0, false);
        add("runwalk_08", "달리기, 걷기", "계단 오르기 (스텝밀 포함)", "", "", 6.8, false);
        add("water_01_l", "수상", "수영 (모든 영법)", "약", "여가 배영", 4.8, false);
        add("water_01_h", "수상", "수영 (모든 영법)", "강", "엘리트 자유형", 14.5, false);
        add("water_02", "수상", "아티스틱 스위밍", "", "", 8.0, true);
        add("water_03", "수상", "다이빙", "", "", 3.0, true);
        add("water_04", "수상", "수구", "", "", 10.0, true);
        add("water_05", "수상", "아쿠아로빅", "", "", 5.5, false);
        add("water_06", "수상", "카약", "", "", 5.0, false);
        add("water_07", "수상", "카누", "", "", 5.8, false);
        add("water_08", "수상", "래프팅", "", "", 5.0, true);
        add("water_09", "수상", "요트, 세일링", "", "", 3.0, false);
        add("water_10", "수상", "윈드서핑", "", "", 5.0, false);
        add("water_11", "수상", "수상스키, 웨이크보드", "", "", 6.0, false);
        add("water_12", "수상", "스킨다이빙", "", "", 11.8, false);
        add("water_13", "수상", "스쿠버다이빙", "", "", 7.0, false);
        add("water_14", "수상", "스노클링", "", "", 5.0, false);
        add("water_15", "수상", "서핑", "", "", 3.0, true);
        add("water_16_l", "수상", "패들보드(SUP)", "약", "", 2.8, false);
        add("water_16_h", "수상", "패들보드(SUP)", "강", "", 11.0, false);
        add("water_17_l", "수상", "아쿠아 워킹, 러닝", "약", "", 2.5, false);
        add("water_17_h", "수상", "아쿠아 워킹, 러닝", "강", "", 9.8, false);
        add("water_18_l", "수상", "아쿠아 사이클링", "약", "", 2.8, false);
        add("water_18_h", "수상", "아쿠아 사이클링", "강", "", 10.3, false);
        add("winter_01", "겨울", "스키, 스노보드", "", "", 6.3, false);
        add("winter_02_l", "겨울", "크로스컨트리 스키 (스키 머신 포함)", "약", "", 6.8, false);
        add("winter_02_h", "겨울", "크로스컨트리 스키 (스키 머신 포함)", "강", "", 16.0, false);
        add("winter_03", "겨울", "산악스키", "", "", 15.5, false);
        add("winter_04_l", "겨울", "롤러스키", "약", "", 5.5, false);
        add("winter_04_h", "겨울", "롤러스키", "강", "", 16.0, false);
        add("winter_05", "겨울", "바이애슬론", "", "", 13.5, false);
        add("winter_06_l", "겨울", "아이스 스케이트", "약", "", 5.5, true);
        add("winter_06_h", "겨울", "아이스 스케이트", "강", "", 11.0, false);
        add("winter_07", "겨울", "스피드스케이팅", "", "", 13.8, false);
        add("winter_08", "겨울", "아이스댄스", "", "", 14.0, false);
        add("winter_09", "겨울", "썰매 (봅슬레이, 루지)", "", "", 7.0, false);
        add("winter_10", "겨울", "스노슈잉", "", "", 5.3, false);
        add("ball_01", "구기, 라켓", "축구", "", "", 7.0, false);
        add("ball_02", "구기, 라켓", "풋살", "", "", 7.8, false);
        add("ball_03", "구기, 라켓", "농구", "", "", 7.5, false);
        add("ball_04", "구기, 라켓", "배구", "", "", 4.0, false);
        add("ball_05", "구기, 라켓", "비치발리볼", "", "", 8.0, true);
        add("ball_06", "구기, 라켓", "야구, 소프트볼", "", "", 5.0, false);
        add("ball_07", "구기, 라켓", "테니스", "", "", 6.8, false);
        add("ball_08", "구기, 라켓", "배드민턴", "", "", 7.0, false);
        add("ball_09", "구기, 라켓", "탁구", "", "", 4.0, false);
        add("ball_10", "구기, 라켓", "스쿼시", "", "", 7.3, false);
        add("ball_11", "구기, 라켓", "라켓볼", "", "", 7.0, false);
        add("ball_12", "구기, 라켓", "패들볼", "", "", 6.0, false);
        add("ball_13", "구기, 라켓", "골프", "", "", 4.5, false);
        add("ball_14", "구기, 라켓", "디스크골프", "", "", 3.8, false);
        add("ball_15", "구기, 라켓", "볼링", "", "", 3.0, false);
        add("ball_16", "구기, 라켓", "미식축구", "", "", 8.0, false);
        add("ball_17", "구기, 라켓", "플래그풋볼", "", "", 8.0, false);
        add("ball_18", "구기, 라켓", "럭비", "", "", 6.3, false);
        add("ball_19", "구기, 라켓", "핸드볼", "", "", 8.0, true);
        add("ball_20", "구기, 라켓", "필드하키", "", "", 7.8, false);
        add("ball_21", "구기, 라켓", "아이스하키", "", "", 8.0, true);
        add("ball_22", "구기, 라켓", "플로어볼", "", "", 10.5, false);
        add("ball_23", "구기, 라켓", "라크로스", "", "", 8.0, true);
        add("ball_24", "구기, 라켓", "크리켓", "", "", 4.8, false);
        add("ball_25", "구기, 라켓", "넷볼", "", "", 7.0, true);
        add("ball_26", "구기, 라켓", "킥볼", "", "", 7.0, true);
        add("ball_27", "구기, 라켓", "월리볼", "", "", 7.0, true);
        add("ball_28", "구기, 라켓", "하이알라이", "", "", 12.0, true);
        add("ball_29", "구기, 라켓", "브룸볼", "", "", 7.0, true);
        add("ball_30", "구기, 라켓", "컬링", "", "", 4.0, true);
        add("ball_31", "구기, 라켓", "프리스비", "", "", 3.0, true);
        add("ball_32", "구기, 라켓", "얼티밋 프리스비", "", "", 8.0, true);
        add("ball_33", "구기, 라켓", "풋백(제기차기류)", "", "", 4.0, true);
        add("ball_34", "구기, 라켓", "폴로", "", "", 8.0, true);
        add("fight_01_l", "격투, 무술", "복싱 (샌드백 포함)", "약", "샌드백", 5.8, false);
        add("fight_01_h", "격투, 무술", "복싱 (샌드백 포함)", "강", "링 경기", 12.3, false);
        add("fight_02", "격투, 무술", "킥복싱", "", "", 7.3, false);
        add("fight_03", "격투, 무술", "태권도", "", "", 14.3, false);
        add("fight_04", "격투, 무술", "유도", "", "", 11.3, false);
        add("fight_05", "격투, 무술", "검도", "", "", 9.6, false);
        add("fight_06", "격투, 무술", "레슬링", "", "", 6.0, true);
        add("fight_07", "격투, 무술", "펜싱", "", "", 6.0, false);
        add("fight_08", "격투, 무술", "쿵후", "", "", 5.5, false);
        add("fight_09", "격투, 무술", "무술 일반(가라테 등)", "", "", 10.3, false);
        add("fight_10", "격투, 무술", "태극권, 기공", "", "", 3.3, false);
        add("etc_01_l", "기타 스포츠", "클라이밍", "약", "", 5.8, false);
        add("etc_01_h", "기타 스포츠", "클라이밍", "강", "", 10.5, false);
        add("etc_02", "기타 스포츠", "볼더링", "", "", 8.8, false);
        add("etc_03", "기타 스포츠", "줄넘기", "", "", 11.8, false);
        add("etc_04", "기타 스포츠", "체조", "", "", 3.8, false);
        add("etc_05", "기타 스포츠", "트램펄린", "", "", 6.3, false);
        add("etc_06", "기타 스포츠", "치어리딩", "", "", 6.0, true);
        add("etc_07", "기타 스포츠", "롤러스케이트", "", "", 7.0, false);
        add("etc_08_l", "기타 스포츠", "인라인스케이트", "약", "시속 14.5km", 7.5, false);
        add("etc_08_h", "기타 스포츠", "인라인스케이트", "강", "시속 24km", 15.5, false);
        add("etc_09", "기타 스포츠", "스케이트보드", "", "", 5.0, true);
        add("etc_10", "기타 스포츠", "롱보드", "", "", 8.3, false);
        add("etc_11", "기타 스포츠", "승마", "", "", 5.5, false);
        add("etc_12", "기타 스포츠", "로데오", "", "", 5.5, true);
        add("etc_13", "기타 스포츠", "양궁", "", "", 4.3, false);
        add("etc_14", "기타 스포츠", "오리엔티어링", "", "", 9.0, true);
        add("etc_15", "기타 스포츠", "육상 투척", "", "", 4.0, true);
        add("etc_16", "기타 스포츠", "육상 도약", "", "", 6.0, true);
        add("etc_17", "기타 스포츠", "허들, 장애물 달리기", "", "", 10.0, true);
        add("etc_18", "기타 스포츠", "알래스카 원주민 게임", "", "", 5.5, true);

        // 예전 버전 코드 (기존 기록, 더미 데이터 호환)
        legacy("walk", "달리기, 걷기", "걷기", 3.5);
        legacy("run", "달리기, 걷기", "달리기", 9.8);
        legacy("bike", "자전거", "자전거", 7.5);
        legacy("hike", "달리기, 걷기", "등산", 6.0);
        legacy("gym", "헬스, 피트니스", "헬스", 5.0);
        legacy("yoga", "헬스, 피트니스", "요가", 2.5);
        legacy("rope", "기타 스포츠", "줄넘기", 11.8);
    }

    public static ExerciseType fromCode(String code) {
        return BY_CODE.get(code);
    }

    /** 고를 수 있는 종목 전체 (파일 순서) */
    public static List<ExerciseType> selectable() {
        return Collections.unmodifiableList(SELECTABLE);
    }

    public static List<String> categories() {
        return Collections.unmodifiableList(CATEGORIES);
    }

    public static List<ExerciseType> inCategory(String category) {
        List<ExerciseType> result = new ArrayList<>();
        for (ExerciseType type : SELECTABLE) {
            if (type.category.equals(category)) result.add(type);
        }
        return result;
    }

    /** 처음 고른 상태로 쓸 종목 */
    public static ExerciseType defaultType() {
        return SELECTABLE.get(0);
    }

    // ---------- 검색 ----------

    /** 한글 첫소리(초성) 19개. 음절 코드에서 (코드 - 0xAC00) / 588 번째 */
    private static final String CHOSEONG = "ㄱㄲㄴㄷㄸㄹㅁㅂㅃㅅㅆㅇㅈㅉㅊㅋㅌㅍㅎ";

    /**
     * 검색어가 이름(강도 포함, 띄어쓰기 무시) 어딘가에 들어 있으면 true.
     * 한글 자음만 친 글자는 음절의 첫소리와도 맞춘다. 예: "ㅂㄷ", "배ㄷ" → 배드민턴.
     * 한글을 치는 도중에는 마지막 글자가 자음만 있는 상태라 이렇게 해야 바로 목록이 뜬다.
     */
    public boolean matches(String query) {
        String q = query == null ? "" : query.replace(" ", "");
        if (q.isEmpty()) return true;
        String text = label().replace(" ", "");
        for (int start = 0; start + q.length() <= text.length(); start++) {
            int i = 0;
            while (i < q.length() && charMatches(q.charAt(i), text.charAt(start + i))) i++;
            if (i == q.length()) return true;
        }
        return false;
    }

    private static boolean charMatches(char q, char c) {
        if (Character.toLowerCase(q) == Character.toLowerCase(c)) return true;
        if (CHOSEONG.indexOf(q) < 0 || c < 0xAC00 || c > 0xD7A3) return false;
        return CHOSEONG.charAt((c - 0xAC00) / 588) == q;
    }
}
