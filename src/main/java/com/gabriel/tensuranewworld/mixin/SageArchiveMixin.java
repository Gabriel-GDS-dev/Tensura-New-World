package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.client.WorldBattleSelectionClient;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.dedek.greatsage.client.SageArchive", remap = false)
public abstract class SageArchiveMixin {
    @Inject(method = "path", at = @At("HEAD"), cancellable = true, remap = false)
    private static void isolateServerWorlds(CallbackInfoReturnable<Path> result) {
        var world = WorldBattleSelectionClient.world();
        if (world == null) { result.setReturnValue(null); return; }
        var minecraft = Minecraft.getInstance();
        if (minecraft.getSingleplayerServer() == null) result.setReturnValue(minecraft.gameDirectory.toPath()
                .resolve("tensuranewworld-worlds").resolve(world.toString()).resolve("greatsage-observations.json"));
    }
    @Inject(method = "adopt", at = @At("HEAD"), cancellable = true, remap = false)
    private static void noGlobalLegacyImport(Path path, CallbackInfo result) {
        // Old global files named after a world can refer to an unrelated, recreated world.
        result.cancel();
    }
}
