package com.mangofit.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** SharedPreferences(mangofit_prefs)에 저장하는 작은 값들. 키는 기획서 '데이터 저장'과 같다. */
final class Prefs {

    static final String FILE = "mangofit_prefs";

    static final String USER_NAME = "user_name";
    static final String FIRST_START_DATE = "first_start_date";
    static final String LEAVES = "leaves";
    static final String MANGO_NO = "mango_no";
    static final String MANGO_START_DATE = "mango_start_date";
    static final String MANGO_EXP = "mango_exp";
    static final String MANGO_LEAVES_EARNED = "mango_leaves_earned";
    static final String EQUIP_HAT = "equip_hat";
    static final String EQUIP_TOP = "equip_top";
    static final String EQUIP_HAND = "equip_hand";
    static final String EQUIP_LEFT_HAND = "equip_left_hand";
    static final String OWNED_ITEMS = "owned_items";
    static final String QUOTA_DATE = "quota_date";
    static final String QUOTA_VALUE = "quota_value";

    private final SharedPreferences sp;

    Prefs(Context context) {
        sp = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    String getUserName() {
        return sp.getString(USER_NAME, "");
    }

    LocalDate getFirstStartDate() {
        return LocalDate.parse(sp.getString(FIRST_START_DATE, LocalDate.now().toString()));
    }

    int getLeaves() {
        return sp.getInt(LEAVES, 0);
    }

    void setLeaves(int leaves) {
        sp.edit().putInt(LEAVES, leaves).apply();
    }

    int getMangoNo() {
        return sp.getInt(MANGO_NO, 1);
    }

    LocalDate getMangoStartDate() {
        return LocalDate.parse(sp.getString(MANGO_START_DATE, LocalDate.now().toString()));
    }

    int getMangoExp() {
        return sp.getInt(MANGO_EXP, 0);
    }

    int getMangoLeavesEarned() {
        return sp.getInt(MANGO_LEAVES_EARNED, 0);
    }

    void setMangoProgress(int exp, int leavesEarned) {
        sp.edit().putInt(MANGO_EXP, exp).putInt(MANGO_LEAVES_EARNED, leavesEarned).apply();
    }

    /** 새 망고 시작: 번호와 시작일을 바꾸고 경험치·번 이파리를 0으로 */
    void startMango(int no, LocalDate startDate) {
        sp.edit()
                .putInt(MANGO_NO, no)
                .putString(MANGO_START_DATE, startDate.toString())
                .putInt(MANGO_EXP, 0)
                .putInt(MANGO_LEAVES_EARNED, 0)
                .apply();
    }

    Equipment getEquipment() {
        return new Equipment(
                sp.getString(EQUIP_HAT, ""),
                sp.getString(EQUIP_TOP, ""),
                sp.getString(EQUIP_HAND, ""), sp.getString(EQUIP_LEFT_HAND, ""));
    }

    void setEquipment(Equipment e) {
        sp.edit()
                .putString(EQUIP_HAT, e.hat == null ? "" : e.hat)
                .putString(EQUIP_TOP, e.top == null ? "" : e.top)
                .putString(EQUIP_HAND, e.hand == null ? "" : e.hand)
                .putString(EQUIP_LEFT_HAND, e.leftHand == null ? "" : e.leftHand)
                .apply();
    }

    List<String> getOwnedItems() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(sp.getString(OWNED_ITEMS, "[]"));
            for (int i = 0; i < array.length(); i++) result.add(array.getString(i));
        } catch (JSONException e) {
            throw new IllegalStateException("owned_items를 읽을 수 없음", e);
        }
        return result;
    }

    void setOwnedItems(List<String> ids) {
        sp.edit().putString(OWNED_ITEMS, new JSONArray(ids).toString()).apply();
    }

    String getQuotaDate() {
        return sp.getString(QUOTA_DATE, "");
    }

    int getQuotaValue() {
        return sp.getInt(QUOTA_VALUE, 0);
    }

    void setQuota(LocalDate date, int value) {
        sp.edit().putString(QUOTA_DATE, date.toString()).putInt(QUOTA_VALUE, value).apply();
    }

    /** 첫 실행: 모든 값을 새로 채운다 */
    void initUser(String name, LocalDate today) {
        sp.edit().clear()
                .putString(USER_NAME, name)
                .putString(FIRST_START_DATE, today.toString())
                .putInt(LEAVES, 0)
                .putInt(MANGO_NO, 1)
                .putString(MANGO_START_DATE, today.toString())
                .putInt(MANGO_EXP, 0)
                .putInt(MANGO_LEAVES_EARNED, 0)
                .putString(EQUIP_HAT, "")
                .putString(EQUIP_TOP, "")
                .putString(EQUIP_HAND, "")
                .putString(EQUIP_LEFT_HAND, "")
                .putString(OWNED_ITEMS, "[]")
                .apply();
    }

    // ---------- 프로필 ----------

    static final String WEIGHT_KG = "weight_kg";
    static final String AVATAR = "avatar";

    void setUserName(String name) {
        sp.edit().putString(USER_NAME, name).apply();
    }

    /** 체중(kg). 입력하지 않았으면 0 */
    float getWeightKg() {
        return sp.getFloat(WEIGHT_KG, 0f);
    }

    void setWeightKg(float kg) {
        sp.edit().putFloat(WEIGHT_KG, kg).apply();
    }

    /** 프로필 사진. 빈 문자열이면 기본 아이콘 */
    String getAvatar() {
        return sp.getString(AVATAR, "");
    }

    void setAvatar(String avatar) {
        sp.edit().putString(AVATAR, avatar).apply();
    }

    // ---------- 운동 타이머 ----------
    // 앱을 나가도 시간이 흐르도록, 흐른 시간 대신 '마지막으로 시작한 시각'과 '그 전까지 쌓인 시간'을 저장한다.

    static final String TIMER_TYPE = "timer_type";
    static final String TIMER_STARTED_AT = "timer_started_at";
    static final String TIMER_ACCUM_MS = "timer_accum_ms";

    /** 타이머가 켜져 있으면(일시정지 포함) 운동 코드, 아니면 빈 문자열 */
    String getTimerType() {
        return sp.getString(TIMER_TYPE, "");
    }

    /** 지금 흐르는 중이면 마지막 시작 시각(epoch ms), 일시정지면 0 */
    long getTimerStartedAt() {
        return sp.getLong(TIMER_STARTED_AT, 0L);
    }

    long getTimerAccumMs() {
        return sp.getLong(TIMER_ACCUM_MS, 0L);
    }

    void setTimer(String type, long startedAt, long accumMs) {
        sp.edit().putString(TIMER_TYPE, type).putLong(TIMER_STARTED_AT, startedAt)
                .putLong(TIMER_ACCUM_MS, accumMs).apply();
    }

    void clearTimer() {
        sp.edit().remove(TIMER_TYPE).remove(TIMER_STARTED_AT).remove(TIMER_ACCUM_MS).apply();
    }

}
