package com.gabriel.tensuranewworld.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class FormCameraTest {
    @Test
    void invalidOrSlimeSizedEyesCannotStayAtTheFeetOfLargerForms() {
        assertEquals(1.7F, FormCamera.eyeHeight(0.1F, 2), 0.001F);
        assertEquals(0.425F, FormCamera.eyeHeight(0, 0.5F), 0.001F);
        assertEquals(1.7F, FormCamera.eyeHeight(Float.NaN, 2), 0.001F);
        assertEquals(1.7F, FormCamera.eyeHeight(4, 2), 0.001F);
        assertEquals(0.3F, FormCamera.eyeHeight(0.3F, 0.5F), 0.001F);
        assertEquals(1.62F, FormCamera.eyeHeight(1.62F, 1.8F), 0.001F);
    }
    @Test
    void framesSmallHumanTallAndWideBodiesAndRestoresSmallForm() {
        float slime = FormCamera.distance(0.5F, 0.5F);
        assertEquals(2, slime);
        assertEquals(4, FormCamera.distance(0.6F, 1.8F), 0.001F);
        assertEquals(10, FormCamera.distance(0.6F, 4.5F), 0.001F);
        assertTrue(FormCamera.distance(3, 1) > 6);
        assertEquals(slime, FormCamera.distance(0.5F, 0.5F));
    }
}
