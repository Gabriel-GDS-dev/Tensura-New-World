package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.client.FormCamera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keep block/entity ray origins and server reach checks at the rendered form's eyes. */
@Mixin(Entity.class)
public abstract class FormSelectionMixin {
    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void tnw$interpolatedEyes(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        tnw$alignEyes(partialTick, cir);
    }

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;", at = @At("RETURN"), cancellable = true)
    private void tnw$currentEyes(CallbackInfoReturnable<Vec3> cir) {
        tnw$alignEyes(1, cir);
    }

    private void tnw$alignEyes(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if ((Object)this instanceof Player player) {
            float eye = FormCamera.measure(player, partialTick).eyeHeight();
            cir.setReturnValue(cir.getReturnValue().add(0, eye - player.getEyeHeight(), 0));
        }
    }
}
