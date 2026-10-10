package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.neoforged.fml.ModList;

@EventBusSubscriber(modid = TensuraNewWorld.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {

    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onGuiLayer(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.screen != null || mc.player.isSpectator()) return;
        if (event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.FOOD_LEVEL)
                || event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.PLAYER_HEALTH)
                || event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.AIR_LEVEL)
                || event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.ARMOR_LEVEL)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDisconnect(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPlayerState.reset();
        WorldBattleSelectionClient.reset();
    }

    @SubscribeEvent
    public static void onConnect(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn event) {
        ClientPlayerState.reset();
        WorldBattleSelectionClient.reset();
        for (String type : java.util.List.of("SageProgressClient", "AnalysisClient", "GreatSageRules", "AnnouncementManager")) {
            com.gabriel.tensuranewworld.integration.ReincarnatedSkills.callStatic("dev.dedek.greatsage.client." + type, "clear");
        }
        com.gabriel.tensuranewworld.integration.ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.SageArchive", "unload");
        com.gabriel.tensuranewworld.integration.ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.AssessMode", "reset");
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (ClientKeyMappings.OPEN_RAPHAEL.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new RaphaelScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen) {
            event.setNewScreen(new TensuraTitleScreen());
            return;
        }
        String screenClass = event.getNewScreen() == null ? "" : event.getNewScreen().getClass().getName();
        if (screenClass.equals("io.github.manasmods.tensura.client.screen.EvolutionScreen")
                || screenClass.equals("io.github.manasmods.tensura.client.screen.MainScreen")) {
            event.setNewScreen(new RaphaelScreen());
        }
    }

    @SubscribeEvent
    @SuppressWarnings("removal")
    public static void onWorldMenuBackground(ScreenEvent.BackgroundRendered event) {
        var screen = event.getScreen();
        if (RpgTheme.themed(screen)) {
            TensuraUi.drawArtworkBackground(event.getGuiGraphics(), screen.width, screen.height, 0xB8121D30);
            GuiGraphics graphics = event.getGuiGraphics();
            RpgTheme.rule(graphics, 12, screen.width - 12, 2);
            RpgTheme.rule(graphics, 12, screen.width - 12, screen.height - 3);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (ModList.get().isLoaded("morph")) {
            return;
        }
        Player player = event.getEntity();
        String formId = ClientPlayerState.getMimicForm(player.getUUID());
        ResourceLocation location = ResourceLocation.tryParse(formId);
        Minecraft minecraft = Minecraft.getInstance();
        if (location == null || minecraft.level == null) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(location).orElse(null);
        if (type == null) {
            return;
        }
        Entity mimic = type.create(minecraft.level);
        if (!(mimic instanceof LivingEntity livingMimic)) {
            return;
        }
        livingMimic.setPos(player.getX(), player.getY(), player.getZ());
        livingMimic.setYRot(player.getYRot());
        livingMimic.setYHeadRot(player.getYHeadRot());
        livingMimic.yRotO = player.yRotO;
        livingMimic.yHeadRotO = player.yHeadRotO;

        event.setCanceled(true);
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        dispatcher.render(livingMimic, 0.0D, 0.0D, 0.0D, player.getYRot(),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

}
