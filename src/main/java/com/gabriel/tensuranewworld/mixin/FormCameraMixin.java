package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.client.FormCamera;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class FormCameraMixin {
    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;
    @Unique private Float tnw$formDistance;

    @Inject(method = "setup", at = @At("HEAD"))
    private void tnw$measure(BlockGetter level, Entity entity, boolean detached, boolean reverse,
                             float partialTick, CallbackInfo ci) {
        tnw$formDistance = null;
        if (entity instanceof Player player) {
            FormCamera.View view = FormCamera.measure(player, partialTick);
            eyeHeight = eyeHeightOld = view.eyeHeight();
            tnw$formDistance = view.distance();
        }
    }

    @ModifyArg(method = "setup", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"), index = 0)
    private float tnw$distance(float vanillaDistance) {
        // Run vanilla's collision sweep with the adjusted distance in both third-person views.
        return tnw$formDistance == null ? vanillaDistance : tnw$formDistance;
    }
}
