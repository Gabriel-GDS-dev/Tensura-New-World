package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.data.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The optional addon's Ultimate rank must not bypass the world's sage stage. */
@Pseudo
@Mixin(targets = "dev.dedek.greatsage.server.SageProgress", remap = false)
public abstract class SageProgressMixin {
    @Inject(method = "sawSkills", at = @At("HEAD"), cancellable = true, remap = false)
    private static void initialSkills(ServerPlayer player, java.util.List<String> ids, CallbackInfo result) {
        if (com.gabriel.tensuranewworld.integration.SageInitialProgress.initializing(player)) {
            if (ids != null) com.gabriel.tensuranewworld.integration.SageInitialProgress.recordStarterSkills(player, ids);
            result.cancel();
        }
    }
    @Inject(method = "has", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gateUltimate(ServerPlayer player, @Coerce Object node, CallbackInfoReturnable<Boolean> result) {
        if ("RANK_ULTIMATE".equals(String.valueOf(node)) && !player.getData(ModAttachments.PLAYER_DATA).isRaphaelAwakened()) result.setReturnValue(false);
    }
    @Inject(method = "unlock", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gateUnlock(ServerPlayer player, String id, CallbackInfo result) {
        if (("rank_ultimate".equalsIgnoreCase(id) || "capstone".equalsIgnoreCase(id))
                && !player.getData(ModAttachments.PLAYER_DATA).isRaphaelAwakened()) {
            player.sendSystemMessage(Component.literal("Desperte como Verdadeiro Lorde Demonio para evoluir Great Sage em Raphael e acessar o rank Supremo."));
            result.cancel();
        }
    }
}
