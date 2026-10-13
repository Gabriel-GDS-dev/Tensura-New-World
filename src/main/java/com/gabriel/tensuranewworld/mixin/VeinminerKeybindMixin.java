package com.gabriel.tensuranewworld.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "de.miraculixx.veinminerClient.constants.NeoForgeKeyBindings", remap = false)
public abstract class VeinminerKeybindMixin {
    @ModifyArg(
            method = "register",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILnet/minecraft/client/KeyMapping$Category;)V",
                    ordinal = 0),
            index = 2,
            remap = false)
    private int tensuranewworld$setDefaultVeinminerKey(int defaultKey) {
        return GLFW.GLFW_KEY_LEFT_SHIFT;
    }
}
