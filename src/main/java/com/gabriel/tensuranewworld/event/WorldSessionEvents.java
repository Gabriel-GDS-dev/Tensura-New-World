package com.gabriel.tensuranewworld.event;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import com.gabriel.tensuranewworld.integration.GreatSageRuntimeScope;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = TensuraNewWorld.MOD_ID)
public final class WorldSessionEvents {
    private WorldSessionEvents() { }
    @SubscribeEvent public static void onStart(ServerAboutToStartEvent event) { GreatSageRuntimeScope.clearAll(); }
    @SubscribeEvent public static void onStop(ServerStoppedEvent event) { GreatSageRuntimeScope.clearAll(); }
    @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { GreatSageRuntimeScope.clearPlayer(event.getEntity().getUUID()); }
}
