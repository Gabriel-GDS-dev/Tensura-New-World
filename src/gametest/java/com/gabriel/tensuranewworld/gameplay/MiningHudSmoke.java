package com.gabriel.tensuranewworld.gameplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in screenshots from a disposable copy of the existing UI test world. */
@EventBusSubscriber(modid = "tensuranewworld", value = Dist.CLIENT)
public final class MiningHudSmoke {
    private static int stage, ticks;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws Exception {
        if (!Boolean.getBoolean("tnw.miningHudSmoke")) return;
        var mc = Minecraft.getInstance();
        if (mc.getOverlay() != null) return;
        if (stage == 0 && mc.screen != null) {
            mc.options.guiScale().set(2); mc.resizeDisplay();
            mc.options.languageCode = "pt_br";
            mc.getLanguageManager().setSelected("pt_br");
            mc.reloadResourcePacks(); stage = 1; return;
        }
        if (stage == 1) {
            stage = 2;
            mc.createWorldOpenFlows().openWorld("UI Preview", () -> { throw new AssertionError("Missing UI test world"); });
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        if (stage == 2) {
            stage = 3; ticks = 0; mc.setScreen(null);
            var id = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                var level = player.serverLevel();
                for (int x = -4; x <= 4; x++) for (int z = -4; z <= 6; z++) for (int y = 79; y <= 84; y++)
                    level.setBlock(new BlockPos(x, y, z), (y == 79 ? Blocks.STONE : Blocks.AIR).defaultBlockState(), 3);
                for (int y = 80; y <= 83; y++) level.setBlock(new BlockPos(0, y, 3), Blocks.DIAMOND_ORE.defaultBlockState(), 3);
                level.getEntities(player, player.getBoundingBox().inflate(25)).forEach(net.minecraft.world.entity.Entity::discard);
                player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                player.getInventory().setItem(0, new ItemStack(Items.IRON_PICKAXE));
                player.getInventory().selected = 0;
                player.connection.teleport(.5, 80, .5, 0, 0);
            });
        }
        if (stage == 3 && ++ticks == 100) {
            if (!(mc.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit)
                    || !mc.level.getBlockState(hit.getBlockPos()).is(Blocks.DIAMOND_ORE)) throw new AssertionError("Ore not under crosshair");
            mc.gui.getChat().clearMessages(true);
            var folder = java.nio.file.Path.of("screenshots", "mining-review"); java.nio.file.Files.createDirectories(folder);
            try (var shot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { shot.writeToFile(folder.resolve("hud.png")); }
            mc.options.guiScale().set(3); mc.resizeDisplay(); stage = 4; ticks = 0;
        } else if (stage == 4 && ++ticks == 30) {
            try (var shot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { shot.writeToFile(java.nio.file.Path.of("screenshots", "mining-review", "hud-compact.png")); }
            org.slf4j.LoggerFactory.getLogger(MiningHudSmoke.class).info("TNW MINING HUD SMOKE COMPLETE");
            mc.stop(); stage = 5;
        }
    }
}
