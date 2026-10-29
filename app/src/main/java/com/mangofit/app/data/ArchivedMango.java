package com.mangofit.app.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

/** mangos.json의 보관된 망고 하나. 보관된 망고의 꾸미기는 바꿀 수 없다. */
public final class ArchivedMango {
    public final int no;
    public final LocalDate startDate;
    public final LocalDate endDate;
    public final Equipment equipped;
    // [임시] 오늘 기록 초기화가 보관된 망고의 기록을 되돌릴 때만 바뀐다
    int exp;
    int leavesEarned;

    public ArchivedMango(int no, LocalDate startDate, LocalDate endDate, int exp, int leavesEarned,
                         Equipment equipped) {
        this.no = no;
        this.startDate = startDate;
        this.endDate = endDate;
        this.exp = exp;
        this.leavesEarned = leavesEarned;
        this.equipped = equipped;
    }

    public int getExp() {
        return exp;
    }

    public int getLeavesEarned() {
        return leavesEarned;
    }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("no", no);
        o.put("start_date", startDate.toString());
        o.put("end_date", endDate.toString());
        o.put("exp", exp);
        o.put("leaves_earned", leavesEarned);
        o.put("equipped", equipped.toJson());
        return o;
    }

    static ArchivedMango fromJson(JSONObject o) throws JSONException {
        return new ArchivedMango(
                o.getInt("no"),
                LocalDate.parse(o.getString("start_date")),
                LocalDate.parse(o.getString("end_date")),
                o.getInt("exp"),
                o.getInt("leaves_earned"),
                Equipment.fromJson(o.optJSONObject("equipped")));
    }
}
