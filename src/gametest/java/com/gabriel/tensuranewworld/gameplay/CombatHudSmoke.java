package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in live HUD capture in the isolated UI Preview test world. */
@EventBusSubscriber(modid = "tensuranewworld", value = Dist.CLIENT)
public final class CombatHudSmoke {
    private static int stage, ticks;
    private static volatile Throwable setupFailure;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) throws Exception {
        if (!Boolean.getBoolean("tnw.combatHudSmoke")) return;
        var mc = Minecraft.getInstance();
        if (mc.getOverlay() != null) return;
        if (setupFailure != null) { mc.stop(); throw new AssertionError("Combat HUD fixture failed", setupFailure); }
        if (stage == 0 && mc.screen != null) {
            mc.options.guiScale().set(2); mc.resizeDisplay();
            mc.options.languageCode = "pt_br";
            mc.getLanguageManager().setSelected("pt_br");
            mc.reloadResourcePacks();
            stage = 1;
            return;
        }
        if (stage == 1) {
            stage = 2;
            mc.createWorldOpenFlows().openWorld("UI Preview", () -> { throw new AssertionError("Missing isolated UI Preview world"); });
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) return;
        if (stage == 2) {
            stage = 3; ticks = 0; mc.setScreen(null);
            var id = mc.player.getUUID();
            mc.getSingleplayerServer().execute(() -> {
                try {
                    var player = mc.getSingleplayerServer().getPlayerList().getPlayer(id);
                    var level = player.serverLevel();
                    for (int x = -5; x <= 5; x++) for (int z = -5; z <= 10; z++) for (int y = 79; y < 85; y++)
                        level.setBlock(new net.minecraft.core.BlockPos(x, y, z), (y == 79 ? net.minecraft.world.level.block.Blocks.STONE : net.minecraft.world.level.block.Blocks.AIR).defaultBlockState(), 3);
                    player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
                    player.connection.teleport(.5, 80, .5, 0, 8);
                    ReincarnatedBridge.writeRace(player, "tensura:slime");
                    player.getData(com.gabriel.tensuranewworld.data.ModAttachments.PLAYER_DATA).setMaxMagicules(10_000_000);
                    ReincarnatedBridge.writeMagicules(player, 10_000_000, 10_000_000);
                    var ability = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getAbilityFrom", player);
                    for (int slot = 0; slot < 3; slot++) {
                        String field = slot == 0 ? "GREAT_SAGE" : slot == 1 ? "PREDATOR" : "GLUTTONY";
                        Object skill = ((java.util.function.Supplier<?>) Class.forName("io.github.manasmods.tensura.registry.skill.UniqueSkills").getField(field).get(null)).get();
                        Object instance = ReincarnatedSkills.call(skill, "createDefaultInstance");
                        ReincarnatedSkills.call(instance, "setMastery", slot == 2 ? -10.0D : 0.0D);
                        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
                        ReincarnatedSkills.call(storage, "learnSkill", instance);
                        Object learned = ((java.util.Optional<?>) ReincarnatedSkills.call(storage, "getSkill", skill)).orElseThrow();
                        ReincarnatedSkills.call(learned, "setMastery", slot == 2 ? -10.0D : 0.0D);
                        ReincarnatedSkills.call(learned, "setCoolDown", slot == 0 ? 600 : 0, 1);
                        ReincarnatedSkills.call(learned, "markDirty");
                        ReincarnatedSkills.call(ability, "setAbilitySlot", slot, skill, 1);
                    }
                    ReincarnatedSkills.call(ability, "markDirty");
                    var cow = net.minecraft.world.entity.EntityType.COW.create(level);
                    cow.setPos(.5, 80, 6.5); cow.setNoAi(true); cow.setHealth(7);
                    cow.setCustomName(net.minecraft.network.chat.Component.literal("Alvo de treinamento"));
                    level.addFreshEntity(cow); ReincarnatedBridge.writeMagicules(cow, 1234, 1234);
                } catch (Exception e) { setupFailure = e; }
            });
        }
        if (stage == 3 && ++ticks == 80) {
            mc.gui.getChat().clearMessages(true);
            var folder = java.nio.file.Path.of("screenshots", "combat-review"); java.nio.file.Files.createDirectories(folder);
            try (var shot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { shot.writeToFile(folder.resolve("hud.png")); }
            mc.options.guiScale().set(3); mc.resizeDisplay(); stage = 4; ticks = 0;
        } else if (stage == 4 && ++ticks == 30) {
            try (var shot = Screenshot.takeScreenshot(mc.getMainRenderTarget())) { shot.writeToFile(java.nio.file.Path.of("screenshots", "combat-review", "hud-compact.png")); }
            org.slf4j.LoggerFactory.getLogger(CombatHudSmoke.class).info("TNW COMBAT HUD SMOKE COMPLETE");
            mc.stop(); stage = 5;
        }
    }
}
