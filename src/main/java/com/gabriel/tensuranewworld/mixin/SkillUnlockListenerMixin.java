package com.gabriel.tensuranewworld.mixin;

import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.dedek.greatsage.server.SkillUnlockListener", remap = false)
public abstract class SkillUnlockListenerMixin {
    @Inject(method = "acquired", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onlyLearnedSkills(ManasSkillInstance instance, ServerPlayer player, CallbackInfoReturnable<Boolean> result) {
        if (instance.getMastery() < 0) result.setReturnValue(false);
    }
}
