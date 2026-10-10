package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.WorldIdentity;
import com.gabriel.tensuranewworld.integration.DefaultGreatSage;
import com.gabriel.tensuranewworld.integration.GreatSageRuntimeScope;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.integration.SageInitialProgress;
import com.gabriel.tensuranewworld.integration.WorldBattleSelection;
import com.gabriel.tensuranewworld.network.WorldSessionPayload;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class WorldIsolationGameTests {
    @GameTest(template = "empty")
    public static void sameAccountStartsFreshWithoutStarterXp(GameTestHelper helper) {
        var account = new GameProfile(UUID.randomUUID(), "WorldIsolation");
        var oldPlayer = new FakePlayer(helper.getLevel(), account);
        ReincarnatedBridge.writeRace(oldPlayer, "tensura:slime");
        DefaultGreatSage.ensure(oldPlayer);
        helper.assertTrue(((Number) ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "xp", oldPlayer)).intValue() == 0, "Free starter must not grant XP");
        SageInitialProgress.completeIfReady(oldPlayer);
        ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "sawSpecies", oldPlayer, "minecraft:zombie");
        helper.assertTrue(((Number) ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "xp", oldPlayer)).intValue() == 25, "Real subsequent observation must still grant XP");
        var newPlayer = new FakePlayer(helper.getLevel(), account);
        ReincarnatedBridge.writeRace(newPlayer, "tensura:slime");
        DefaultGreatSage.ensure(newPlayer);
        helper.assertTrue(((Number) ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "xp", newPlayer)).intValue() == 0, "Same account's new player save must start at zero");
        helper.assertTrue(WorldBattleSelection.get(newPlayer).isEmpty(), "New save must not inherit battle skills");
        helper.assertTrue(((Number) ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "xp", oldPlayer)).intValue() == 25, "Existing world progress must not be erased");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void battleSelectionRejectsGhostsAndWrongWorld(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BattleIsolation"));
        DefaultGreatSage.ensure(player);
        var world = WorldIdentity.get(player.getServer());
        helper.assertTrue(WorldBattleSelection.update(player, world, "tensura:great_sage", true), "Learned native skill must be selectable");
        WorldBattleSelection.update(player, world, "tensura:great_sage", true);
        helper.assertTrue(WorldBattleSelection.get(player).size() == 1, "Duplicates cannot inflate count");
        helper.assertFalse(WorldBattleSelection.update(player, world, "tensura:creator", true), "Unowned skill must not be counted");
        helper.assertFalse(WorldBattleSelection.update(player, world, "tensura:magic_resistance", true), "Resistance cannot be a battle selection");
        helper.assertFalse(WorldBattleSelection.update(player, UUID.randomUUID(), "tensura:great_sage", false), "Old world's request must be rejected");
        helper.assertTrue(WorldBattleSelection.get(player).size() == 1, "Rejected request must preserve current world's data");
        var clone = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BattleClone"));
        DefaultGreatSage.ensure(clone);
        WorldBattleSelection.clone(player, clone);
        helper.assertTrue(WorldBattleSelection.get(clone).size() == 1, "Death/respawn must preserve selection in the same world");
        clone.getPersistentData().getCompound("tnw_battle_selection").putUUID("World", UUID.randomUUID());
        helper.assertTrue(WorldBattleSelection.get(clone).isEmpty(), "Foreign saved selection must not leak into current world");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        var packet = new WorldSessionPayload(world, WorldBattleSelection.get(player));
        WorldSessionPayload.STREAM_CODEC.encode(buffer, packet);
        helper.assertTrue(WorldSessionPayload.STREAM_CODEC.decode(buffer).equals(packet), "World token and selection must survive synchronization");
        buffer.release();
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void identityPersistsAndPendingEvolutionCannotCrossWorlds(GameTestHelper helper) throws Exception {
        var first = new WorldIdentity();
        var second = new WorldIdentity();
        helper.assertTrue(!first.id().equals(second.id()), "Every new world must have its own identity");
        helper.assertTrue(WorldIdentity.load(first.save(new CompoundTag(), helper.getLevel().registryAccess()), helper.getLevel().registryAccess()).id().equals(first.id()), "Reopening a world must retain its identity");
        var id = UUID.randomUUID();
        var service = Class.forName("dev.dedek.greatsage.server.RaphaelEvolutionService");
        var pending = service.getDeclaredField("PENDING"); pending.setAccessible(true);
        var evolving = service.getDeclaredField("EVOLVING"); evolving.setAccessible(true);
        ((java.util.Map<UUID, Integer>) pending.get(null)).put(id, 2);
        ((java.util.Set<UUID>) evolving.get(null)).add(id);
        GreatSageRuntimeScope.clearPlayer(id);
        helper.assertTrue(!((java.util.Map<?, ?>) pending.get(null)).containsKey(id) && !((java.util.Set<?>) evolving.get(null)).contains(id), "Logout must cancel the previous session's pending evolution");
        helper.succeed();
    }
}
