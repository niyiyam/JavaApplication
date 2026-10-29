package com.mangofit.app.data;

import com.mangofit.app.logic.Slot;

import org.json.JSONException;
import org.json.JSONObject;

/** 부위별 장착 아이템 ID. 비어 있으면 null. */
public final class Equipment {
    public final String hat;
    public final String top;
    public final String hand;

    public Equipment(String hat, String top, String hand) {
        this.hat = emptyToNull(hat);
        this.top = emptyToNull(top);
        this.hand = emptyToNull(hand);
    }

    public static Equipment empty() {
        return new Equipment(null, null, null);
    }

    public String get(Slot slot) {
        switch (slot) {
            case HAT: return hat;
            case TOP: return top;
            default: return hand;
        }
    }

    public Equipment with(Slot slot, String itemId) {
        return new Equipment(
                slot == Slot.HAT ? itemId : hat,
                slot == Slot.TOP ? itemId : top,
                slot == Slot.HAND ? itemId : hand);
    }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("hat", hat == null ? JSONObject.NULL : hat);
        o.put("top", top == null ? JSONObject.NULL : top);
        o.put("hand", hand == null ? JSONObject.NULL : hand);
        return o;
    }

    static Equipment fromJson(JSONObject o) {
        if (o == null) return empty();
        return new Equipment(optId(o, "hat"), optId(o, "top"), optId(o, "hand"));
    }

    private static String optId(JSONObject o, String key) {
        return o.isNull(key) ? null : o.optString(key, null);
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
