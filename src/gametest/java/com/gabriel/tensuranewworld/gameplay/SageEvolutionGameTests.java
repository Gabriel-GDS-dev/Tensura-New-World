package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.integration.DefaultGreatSage;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.integration.SageEvolution;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class SageEvolutionGameTests {
    private static Object learned(ServerPlayer player, String id) {
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
        return ((Collection<?>) ReincarnatedSkills.call(storage, "getLearnedSkills")).stream()
                .filter(instance -> id.equals(String.valueOf(ReincarnatedSkills.call(instance, "getSkillId")))).findFirst().orElse(null);
    }
    @GameTest(template = "empty")
    public static void harvestEvolvesSageAndUltimateSlimeNamesCiel(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "SageEvolution"));
        var data = player.getData(ModAttachments.PLAYER_DATA);
        data.setRaceId("tensura:slime");
        ReincarnatedBridge.writeRace(player, data.getRaceId());
        DefaultGreatSage.ensure(player);
        Object sage = learned(player, "tensura:great_sage");
        ReincarnatedSkills.call(sage, "setMastery", 123.0D);
        ReincarnatedSkills.call(sage, "setCoolDown", 400, 0);
        ReincarnatedSkills.call(sage, "setToggled", true);
        ReincarnatedSkills.call(sage, "onToggleOn", player);
        Object ability = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getAbilityFrom", player);
        var presets = (java.util.List<?>) ReincarnatedSkills.call(ability, "getPresets");
        for (int preset = 0; preset < presets.size(); preset++) {
            ReincarnatedSkills.call(ability, "setAbilitySlot", preset, 0, ReincarnatedSkills.call(sage, "getSkill"), 1);
        }
        ((net.minecraft.nbt.CompoundTag) ReincarnatedSkills.call(sage, "getOrCreateTag")).putString("TestStorage", "retained");
        data.setDemonLordSeed(true);
        SageEvolution.evaluate(player, data);
        helper.assertTrue(learned(player, "tensuranewworld:raphael") == null && learned(player, "tensuranewworld:ciel") == null, "Seed alone cannot unlock evolved powers");
        HarvestFestivalManager.debugComplete(player, data);
        Object raphael = learned(player, "tensuranewworld:raphael");
        helper.assertTrue(raphael != null && data.isRaphaelAwakened(), "Harvest must grant actual native Raphael");
        helper.assertTrue(Boolean.TRUE.equals(ReincarnatedSkills.call(raphael, "isToggled")), "Evolution must preserve the active passive");
        helper.assertTrue(learned(player, "tensura:great_sage") == null, "Raphael must replace Great Sage");
        helper.assertTrue(((Number) ReincarnatedSkills.call(raphael, "getMastery")).doubleValue() == 123, "Evolution must retain mastery");
        helper.assertTrue(((Number) ReincarnatedSkills.call(raphael, "getCoolDown", 0)).intValue() == 400, "Evolution must retain cooldown");
        helper.assertTrue(!data.isCielAwakened(), "Demon Lord alone must not unlock Ciel");
        data.setRaceId("tensura:divine_dragon");
        SageEvolution.evaluate(player, data);
        helper.assertTrue(!data.isCielAwakened(), "Divine Dragon is not Rimuru's True Dragon milestone");
        ReincarnatedBridge.writeRace(player, "tensura:god_slime");
        ReincarnatedBridge.syncFromBase(player, data);
        helper.assertTrue(data.getRaceId().equals("tensura:god_slime"), "Native Ultimate Slime progression must not be reverted");
        SageEvolution.evaluate(player, data);
        Object ciel = learned(player, "tensuranewworld:ciel");
        helper.assertTrue(ciel != null && data.isCielAwakened(), "Ultimate Slime must name and awaken Ciel");
        for (int preset = 0; preset < presets.size(); preset++) {
            Object equipped = ReincarnatedSkills.call(ability, "getAbilitySlot", preset, 0);
            helper.assertTrue("tensuranewworld:ciel".equals(String.valueOf(ReincarnatedSkills.call(ReincarnatedSkills.call(equipped, "getSkill"), "getRegistryName")))
                    && ((Number) ReincarnatedSkills.call(equipped, "getMode")).intValue() == 1, "Every preset must retain its slot and mode across both evolutions");
        }
        helper.assertTrue(learned(player, "tensuranewworld:raphael") == null, "Ciel must replace Raphael");
        helper.assertTrue("retained".equals(((net.minecraft.nbt.CompoundTag) ReincarnatedSkills.call(ciel, "getOrCreateTag")).getString("TestStorage")), "Evolution must preserve sage storage");
        helper.assertFalse(DefaultGreatSage.ensure(player), "Default starter grant must not undo Ciel");
        var view = ReincarnatedSkills.snapshot(player, data).stream().filter(row -> row.id().equals("tensuranewworld:ciel")).findFirst().orElseThrow();
        helper.assertTrue(view.category() == com.gabriel.tensuranewworld.registry.SkillCategory.MANAS && view.modes().size() == 4, "Ciel must have usable modes in Manas");
        helper.assertTrue(view.modes().get(3).magiculeCost() == 15000, "Ciel optimization must have costly usage");
        Object breath = ClassSkill.skill("io.github.manasmods.tensura.registry.skill.IntrinsicSkills", "PARALYSING_BREATH");
        Object freshBreath = ReincarnatedSkills.call(breath, "createDefaultInstance");
        ReincarnatedSkills.call(freshBreath, "setMastery", 0.0D);
        ((net.minecraft.nbt.CompoundTag) ReincarnatedSkills.call(freshBreath, "getOrCreateTag")).putBoolean("NoMagiculeCost", true);
        ReincarnatedSkills.callStatic("io.github.manasmods.tensura.ability.SkillHelper", "learnSkill", player, freshBreath);
        Object other = learned(player, "tensura:paralysing_breath");
        helper.assertTrue(other != null, "Optimization target must be learned");
        ReincarnatedSkills.call(other, "setCoolDown", 400, 0);
        ReincarnatedBridge.writeMagicules(player, 0, 30000);
        ReincarnatedSkills.call(ciel, "onPressed", player, 0, 3);
        helper.assertTrue(((Number) ReincarnatedSkills.call(other, "getCoolDown", 0)).intValue() == 400, "Insufficient energy must not optimize");
        ReincarnatedBridge.writeMagicules(player, 30000, 30000);
        ReincarnatedSkills.call(ciel, "onPressed", player, 0, 3);
        helper.assertTrue(((Number) ReincarnatedSkills.call(other, "getCoolDown", 0)).intValue() == 200, "Ciel must actually optimize other cooldowns");
        helper.assertTrue(((Number) ReincarnatedSkills.call(ciel, "getCoolDown", 3)).intValue() == 1200, "Optimization cannot reset its own cooldown");
        Object existence = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.storage.TensuraStorages", "getExistenceFrom", player);
        helper.assertTrue(((Number) ReincarnatedSkills.call(existence, "getMagicule")).doubleValue() == 15000, "Ciel must actually spend 15000 magicules");
        SageEvolution.evaluate(player, data);
        helper.assertTrue(((Number) ReincarnatedSkills.call(ciel, "getCoolDown", 3)).intValue() == 1200, "Repeated evaluation must not reset evolved skills");
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void addonUltimateRankRequiresRaphael(GameTestHelper helper) throws Exception {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "RankGate"));
        var data = player.getData(ModAttachments.PLAYER_DATA);
        Class<?> nodes = Class.forName("dev.dedek.greatsage.SageNode");
        Object ultimate = nodes.getField("RANK_ULTIMATE").get(null);
        var persisted = new net.minecraft.nbt.CompoundTag();
        var progress = new net.minecraft.nbt.CompoundTag();
        var unlocked = new net.minecraft.nbt.ListTag();
        unlocked.add(net.minecraft.nbt.StringTag.valueOf("rank_ultimate"));
        progress.put("unlocked", unlocked);
        persisted.put("greatsage_progress", progress);
        player.getPersistentData().put("PlayerPersisted", persisted);
        helper.assertTrue(Boolean.FALSE.equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "has", player, ultimate)), "Stored tree unlock cannot bypass stage gate");
        data.setTrueDemonLord(true);
        SageEvolution.evaluate(player, data);
        helper.assertTrue(Boolean.TRUE.equals(ReincarnatedSkills.callStatic("dev.dedek.greatsage.server.SageProgress", "has", player, ultimate)), "Existing tree unlock becomes usable with Raphael");
        helper.succeed();
    }
    private static final class ClassSkill {
        static Object skill(String type, String name) {
            try { return ((java.util.function.Supplier<?>) Class.forName(type).getField(name).get(null)).get(); }
            catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
        }
    }
}
