package com.mangofit.app.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

/** workouts.json의 기록 하나. 받은 이파리·경험치를 기록마다 저장한다. */
public final class WorkoutRecord {
    public final int id;
    public final String createdAt;
    public final LocalDate date;
    public final String type;
    public final int minutes;
    public final int score;
    public final int leaves;
    public final int exp;
    public final int mangoNo;

    public WorkoutRecord(int id, String createdAt, LocalDate date, String type, int minutes,
                         int score, int leaves, int exp, int mangoNo) {
        this.id = id;
        this.createdAt = createdAt;
        this.date = date;
        this.type = type;
        this.minutes = minutes;
        this.score = score;
        this.leaves = leaves;
        this.exp = exp;
        this.mangoNo = mangoNo;
    }

    JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("created_at", createdAt);
        o.put("date", date.toString());
        o.put("type", type);
        o.put("minutes", minutes);
        o.put("score", score);
        o.put("leaves", leaves);
        o.put("exp", exp);
        o.put("mango_no", mangoNo);
        return o;
    }

    static WorkoutRecord fromJson(JSONObject o) throws JSONException {
        return new WorkoutRecord(
                o.getInt("id"),
                o.optString("created_at", ""),
                LocalDate.parse(o.getString("date")),
                o.getString("type"),
                o.getInt("minutes"),
                o.getInt("score"),
                o.getInt("leaves"),
                o.getInt("exp"),
                o.getInt("mango_no"));
    }
}
