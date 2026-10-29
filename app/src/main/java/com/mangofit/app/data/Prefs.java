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
                sp.getString(EQUIP_HAND, ""));
    }

    void setEquipment(Equipment e) {
        sp.edit()
                .putString(EQUIP_HAT, e.hat == null ? "" : e.hat)
                .putString(EQUIP_TOP, e.top == null ? "" : e.top)
                .putString(EQUIP_HAND, e.hand == null ? "" : e.hand)
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
                .putString(OWNED_ITEMS, "[]")
                .apply();
    }

    /** 전체 값을 JSON 객체 내용으로 덮어쓴다. 할당량은 다시 고정하도록 비운다. */
    void importFrom(JSONObject o) throws JSONException {
        JSONArray owned = o.optJSONArray(OWNED_ITEMS);
        sp.edit().clear()
                .putString(USER_NAME, o.getString(USER_NAME))
                .putString(FIRST_START_DATE, o.getString(FIRST_START_DATE))
                .putInt(LEAVES, o.getInt(LEAVES))
                .putInt(MANGO_NO, o.getInt(MANGO_NO))
                .putString(MANGO_START_DATE, o.getString(MANGO_START_DATE))
                .putInt(MANGO_EXP, o.getInt(MANGO_EXP))
                .putInt(MANGO_LEAVES_EARNED, o.getInt(MANGO_LEAVES_EARNED))
                .putString(EQUIP_HAT, o.optString(EQUIP_HAT, ""))
                .putString(EQUIP_TOP, o.optString(EQUIP_TOP, ""))
                .putString(EQUIP_HAND, o.optString(EQUIP_HAND, ""))
                .putString(OWNED_ITEMS, owned == null ? "[]" : owned.toString())
                .apply();
    }
}
