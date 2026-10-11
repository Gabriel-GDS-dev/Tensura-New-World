package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.integration.MorphAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "me.ichun.mods.morph.common.morph.MorphHandler", remap = false)
public abstract class MorphAccessMixin {
    @Inject(method = {"canMorph", "canShowMorphSelector"}, at = @At("HEAD"), cancellable = true, remap = false)
    private void tnw$canMorph(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!MorphAccess.allowed(player)) cir.setReturnValue(false);
    }
    @Inject(method = "canAcquireMorph", at = @At("HEAD"), cancellable = true, remap = false)
    private void tnw$canAcquire(Player player, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if (!MorphAccess.allowed(player)) cir.setReturnValue(false);
    }
    @Inject(method = "acquireMorph", at = @At("HEAD"), cancellable = true, remap = false)
    private void tnw$acquire(ServerPlayer player, @Coerce Object variant, CallbackInfoReturnable<Boolean> cir) {
        if (!MorphAccess.allowed(player)) cir.setReturnValue(false);
    }
    @Inject(method = "morphTo", at = @At("HEAD"), cancellable = true, remap = false)
    private void tnw$morph(ServerPlayer player, @Coerce Object variant, CallbackInfoReturnable<Boolean> cir) {
        if (!MorphAccess.allowed(player) && !MorphAccess.originalBody(player, variant)) cir.setReturnValue(false);
    }
}
