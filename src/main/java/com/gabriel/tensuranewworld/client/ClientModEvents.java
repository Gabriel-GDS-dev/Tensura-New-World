package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = TensuraNewWorld.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyMappings.OPEN_RAPHAEL);
    }
    @SubscribeEvent
    public static void registerHud(net.neoforged.neoforge.client.event.RegisterGuiLayersEvent event) {
        event.registerAboveAll(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(TensuraNewWorld.MOD_ID, "survival"),
                (graphics, delta) -> {
                    var mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.player != null && !mc.options.hideGui && mc.screen == null) SurvivalHud.render(graphics);
                });
    }
}
