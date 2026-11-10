package com.mangofit.app.data;

import com.mangofit.app.logic.Slot;
import org.junit.Test;
import static org.junit.Assert.*;

public class EquipmentTest {
    @Test public void legacyEquipmentKeepsRightHandAndHasEmptyLeftHand() {
        Equipment old = new Equipment("hat_a_red", "top_a_blue", "hand_b_green");
        assertEquals("hand_b_green", old.get(Slot.HAND));
        assertNull(old.get(Slot.LEFT_HAND));
    }

    @Test public void changingAndRemovingLeftHandPreservesOtherSlots() {
        Equipment original = new Equipment("hat_a_red", "outfit_suit", "right_camera");
        Equipment dressed = original.with(Slot.LEFT_HAND, "left_dumbbell");
        assertNull(original.leftHand);
        assertEquals("left_dumbbell", dressed.leftHand);
        Equipment cleared = dressed.with(Slot.LEFT_HAND, null);
        assertNull(cleared.leftHand);
        assertEquals(original.hat, cleared.hat);
        assertEquals(original.top, cleared.top);
        assertEquals(original.hand, cleared.hand);
    }
}
