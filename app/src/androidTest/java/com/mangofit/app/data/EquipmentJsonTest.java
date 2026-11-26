package com.mangofit.app.data;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EquipmentJsonTest {
    @Test public void oldArchivedEquipmentLoadsWithoutLeftHand() throws Exception {
        Equipment e = Equipment.fromJson(new JSONObject(
                "{\"hat\":\"hat_a_red\",\"top\":null,\"hand\":\"hand_b_green\"}"));
        assertEquals("hat_a_red", e.hat);
        assertEquals("hand_b_green", e.hand);
        assertNull(e.leftHand);
    }

    @Test public void bothHandsAndOutfitSurviveJsonRoundTrip() throws Exception {
        Equipment before = new Equipment(null, "outfit_student", "right_camera", "left_dumbbell");
        Equipment after = Equipment.fromJson(new JSONObject(before.toJson().toString()));
        assertEquals(before.top, after.top);
        assertEquals(before.hand, after.hand);
        assertEquals(before.leftHand, after.leftHand);
        assertNull(after.hat);
    }
}
