package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.client.ClientGameEvents;
import com.gabriel.tensuranewworld.client.WorldBattleSelectionClient;
import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.network.WorldSessionPayload;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opt-in client launch verifies the optional client mixins against the real addon. */
@EventBusSubscriber(modid = "tensuranewworld", value = Dist.CLIENT)
public final class ClientIsolationSmoke {
    private static boolean done;
    @SubscribeEvent
    public static void check(ClientTickEvent.Post event) throws Exception {
        if (done || !Boolean.getBoolean("tnw.clientIsolationSmoke") || Minecraft.getInstance().screen == null) return;
        done = true;
        // Loading the actual Camera verifies its client-only mixin transforms successfully.
        net.minecraft.client.Camera.class.getDeclaredField("tnw$formDistance");
        org.slf4j.LoggerFactory.getLogger(ClientIsolationSmoke.class).info("TNW FORM CAMERA MIXIN LOADED");
        Object config = Class.forName("dev.dedek.greatsage.GreatSageConfig").getField("BATTLE_SKILLS").get(null);
        Object before = ReincarnatedSkills.call(config, "get");
        try {
            ReincarnatedSkills.call(config, "set", List.of("tensura:creator", "tensura:great_sage", "tensura:degenerate"));
            WorldBattleSelectionClient.reset();
            if (!Integer.valueOf(0).equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.BattleSet", "size"))) throw new AssertionError("Global config leaked into battle count");
            var oldWorld = UUID.randomUUID();
            WorldBattleSelectionClient.accept(new WorldSessionPayload(oldWorld, List.of("tensura:creator")));
            Object progress = Class.forName("dev.dedek.greatsage.network.ProgressPayload")
                    .getConstructor(int.class, int.class, int.class, int.class, int.class, List.class, List.class, String.class)
                    .newInstance(350, 4, 12, 0, 500, List.of("reach_1"), List.of(), "");
            ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.SageProgressClient", "accept", progress);
            ClientGameEvents.onConnect(null);
            if (WorldBattleSelectionClient.world() != null) throw new AssertionError("Old world token survived reconnect");
            if (!Integer.valueOf(0).equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.SageProgressClient", "xp"))) throw new AssertionError("Old world XP survived reconnect");
            if (!Integer.valueOf(1).equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.SageProgressClient", "level"))) throw new AssertionError("Old world level survived reconnect");
            var newWorld = UUID.randomUUID();
            WorldBattleSelectionClient.accept(new WorldSessionPayload(newWorld, List.of()));
            if (!Integer.valueOf(0).equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.BattleSet", "size"))) throw new AssertionError("New world inherited selections");
            if (!Boolean.TRUE.equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.client.SageArchive", "isEmpty"))) throw new AssertionError("New world inherited observations");
            org.slf4j.LoggerFactory.getLogger(ClientIsolationSmoke.class).info("TNW CLIENT ISOLATION SMOKE PASSED: battle config ignored; reconnect resets XP/level; world selection and archive empty.");
        } finally {
            ReincarnatedSkills.call(config, "set", before);
            WorldBattleSelectionClient.reset();
        }
        Minecraft.getInstance().stop();
    }
}
