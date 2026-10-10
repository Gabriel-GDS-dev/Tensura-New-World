package com.gabriel.tensuranewworld.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ClientKeyMappings {
    public static final KeyMapping OPEN_RAPHAEL = new KeyMapping(
            "key.tensuranewworld.open_raphael",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.tensuranewworld");

    private ClientKeyMappings() {
    }
}
