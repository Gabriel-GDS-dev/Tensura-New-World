package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import com.mojang.authlib.GameProfile;
import io.github.manasmods.manascore.race.api.RaceAPI;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class RaceSyncGameTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void everyNativeRaceReplacesStaleSlimeWithoutChangingNativeSelection(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().registryOrThrow(RaceAPI.getRaceRegistryKey());
        int checked = 0;
        for (var id : registry.keySet()) {
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "RaceSync"));
            var data = TensuraPlayerData.load(new TensuraPlayerData().save());
            RaceEvolutionManager.evaluate(player, data); // Login before the race selection.
            data.setPendingEvolution("named_slime");
            helper.assertTrue(ReincarnatedBridge.writeRace(player, id.toString()), "Native selection must work: " + id);
            helper.assertTrue(ReincarnatedBridge.syncFromBase(player, data), "Sync must work: " + id);
            helper.assertTrue(data.getRaceId().equals(id.toString()), "Raphael must receive the selected race: " + id);
            helper.assertTrue(ReincarnatedBridge.raceId(player).orElseThrow().equals(id.toString()), "Sync cannot overwrite native race: " + id);
            helper.assertTrue(data.getEvolutionStage().equals(EvolutionRegistry.inferStage(id.toString())), "Evolution must follow selected race: " + id);
            if (!id.toString().equals("tensura:slime")) helper.assertTrue(data.getPendingEvolution().isEmpty(), "Old evolution must be cleared: " + id);
            var restored = TensuraPlayerData.load(data.save());
            var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
            try {
                var codec = com.gabriel.tensuranewworld.network.SyncPlayerDataPayload.STREAM_CODEC;
                codec.encode(buffer, new com.gabriel.tensuranewworld.network.SyncPlayerDataPayload(data.save()));
                var clientData = TensuraPlayerData.load(codec.decode(buffer).data());
                helper.assertTrue(clientData.getRaceId().equals(id.toString()), "Raphael client packet must retain race: " + id);
            } finally {
                buffer.release();
            }
            ReincarnatedBridge.syncFromBase(player, restored);
            helper.assertTrue(restored.getRaceId().equals(id.toString()), "Saved race must survive reload: " + id);
            // Also exercise subsequent selection/evolution with an established local race.
            helper.assertTrue(ReincarnatedBridge.writeRace(player, "tensura:human"), "Switch must work");
            ReincarnatedBridge.syncFromBase(player, restored);
            helper.assertTrue(restored.getRaceId().equals("tensura:human"), "Later native changes must reach Raphael: " + id);
            checked++;
        }
        helper.assertTrue(checked > 40, "Must exercise the full native registry, got " + checked);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unchangedSlimeRetainsNamedMilestone(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NamedSync"));
        helper.assertTrue(ReincarnatedBridge.writeRace(player, "tensura:slime"), "Slime selection must work");
        var data = new TensuraPlayerData();
        data.setEvolutionStage("named_slime");
        ReincarnatedBridge.syncFromBase(player, data);
        helper.assertTrue(data.getEvolutionStage().equals("named_slime"), "Unchanged race must retain local milestone");
        helper.succeed();
    }
}
