package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.client.WorldBattleSelectionClient;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.dedek.greatsage.client.BattleSet", remap = false)
public abstract class BattleSetMixin {
    @Inject(method = "all", at = @At("HEAD"), cancellable = true, remap = false)
    private static void worldSelection(CallbackInfoReturnable<List<String>> result) { result.setReturnValue(WorldBattleSelectionClient.all()); }
    @Inject(method = "toggle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void toggleSelection(ResourceLocation id, CallbackInfo result) { WorldBattleSelectionClient.toggle(id); result.cancel(); }
    @Inject(method = "clear", at = @At("HEAD"), cancellable = true, remap = false)
    private static void clearSelection(CallbackInfo result) { WorldBattleSelectionClient.clear(); result.cancel(); }
}
