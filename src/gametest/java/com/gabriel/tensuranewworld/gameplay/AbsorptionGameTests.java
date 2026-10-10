package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.integration.MonsterSkillAbsorption;
import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class AbsorptionGameTests {
    @GameTest(template = "empty")
    public static void unlockCatalogAndNativeGreatSageCommand(GameTestHelper helper) throws Exception {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "UnlockGuideTest"));
        var data = player.getData(ModAttachments.PLAYER_DATA);
        var rows = ReincarnatedSkills.snapshot(player, data);
        helper.assertTrue(rows.size() >= 357, "All native and addon abilities must appear before acquisition: " + rows.size());
        var sage = rows.stream().filter(row -> row.id().equals("tensura:great_sage")).findFirst().orElseThrow();
        helper.assertTrue(sage.unlockGuide().contains("automaticamente"), "Default free acquisition must be shown");
        helper.assertFalse(sage.modes().stream().anyMatch(mode -> mode.slottable()), "Catalog must not grant locked skills");
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        var tag = new net.minecraft.nbt.CompoundTag();
        var list = new net.minecraft.nbt.ListTag();
        for (var row : rows) {
            helper.assertTrue(com.gabriel.tensuranewworld.integration.SkillUnlockGuide.contains(row.id()), "Missing guide: " + row.id());
            helper.assertFalse(row.unlockGuide().contains("valor da configuracao do servidor"), "Unresolved config: " + row.id());
            helper.assertTrue(com.gabriel.tensuranewworld.data.SkillView.load(row.save()).unlockGuide().equals(row.unlockGuide()), "Guide must survive network NBT");
            list.add(row.save());
        }
        tag.put("Skills", list);
        buffer.writeNbt(tag);
        helper.assertTrue(buffer.readableBytes() < 900000, "Full catalog must fit payload budget: " + buffer.readableBytes());
        buffer.release();
        player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), "tensura edit ability grant @s tensura:great_sage");
        sage = ReincarnatedSkills.snapshot(player, data).stream().filter(row -> row.id().equals("tensura:great_sage")).findFirst().orElseThrow();
        helper.assertTrue(sage.modes().stream().anyMatch(mode -> mode.slottable()), "Documented command must grant usable native Great Sage");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyRaceGetsGreatSageWithoutCommands(GameTestHelper helper) throws Exception {
        for (String race : java.util.List.of("tensura:slime", "tensura:human")) {
            var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "DefaultSage"));
            var data = player.getData(ModAttachments.PLAYER_DATA);
            data.setRaceId(race);
            Object before = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.util.EnergyHelper", "getBaseMaxMagicule", player);
            helper.assertTrue(before instanceof Number, "Native capacity API must resolve");
            com.gabriel.tensuranewworld.event.GameplayEvents.onLogin(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent(player));
            var sage = ReincarnatedSkills.snapshot(player, data).stream().filter(row -> row.id().equals("tensura:great_sage")).findFirst().orElseThrow();
            helper.assertTrue(sage.mastery() >= 0 && !sage.temporary() && sage.modes().stream().anyMatch(mode -> mode.slottable()), "Login must grant usable permanent Great Sage for " + race);
            helper.assertTrue(java.util.Objects.equals(before, ReincarnatedSkills.callStatic("io.github.manasmods.tensura.util.EnergyHelper", "getBaseMaxMagicule", player)), "Starter skill must not reduce MP capacity");
            Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
            for (Object instance : (java.util.Collection<?>) ReincarnatedSkills.call(storage, "getLearnedSkills")) {
                if ("tensura:great_sage".equals(String.valueOf(ReincarnatedSkills.call(instance, "getSkillId")))) ReincarnatedSkills.call(instance, "setMastery", 123.0D);
            }
            helper.assertFalse(com.gabriel.tensuranewworld.integration.DefaultGreatSage.ensure(player), "Repeated grant must be a no-op");
            sage = ReincarnatedSkills.snapshot(player, data).stream().filter(row -> row.id().equals("tensura:great_sage")).findFirst().orElseThrow();
            helper.assertTrue(sage.mastery() == 123, "Existing mastery must be preserved");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void centipedeBreathBecomesPermanentUsableSkill(GameTestHelper helper) throws Exception {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "AbsorptionTest"));
        var data = player.getData(ModAttachments.PLAYER_DATA);
        data.setRaceId("tensura:slime");
        var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("tensura:evil_centipede"));
        var mob = (Mob) helper.spawn(type, 1, 2, 1);
        Object skill = Class.forName("io.github.manasmods.tensura.registry.skill.IntrinsicSkills")
                .getField("PARALYSING_BREATH").get(null);
        skill = ((java.util.function.Supplier<?>) skill).get();
        Class.forName("io.github.manasmods.tensura.ability.SkillHelper").getMethod("learnSkill",
                net.minecraft.world.entity.LivingEntity.class,
                Class.forName("io.github.manasmods.manascore.skill.api.ManasSkill")).invoke(null, mob, skill);
        Object source = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", mob);
        Object learned = ReincarnatedSkills.call(source, "getLearnedSkills");
        helper.assertTrue(learned instanceof java.util.Collection<?> skills && !skills.isEmpty(),
                "Monster must have its native breath: " + learned);
        helper.assertTrue(MonsterSkillAbsorption.canAbsorb(data), "Player must be eligible");
        helper.assertTrue("INTRINSIC".equals(String.valueOf(ReincarnatedSkills.call(skill, "getType"))),
                "Native type: " + ReincarnatedSkills.call(skill, "getType"));
        int absorbed = MonsterSkillAbsorption.absorb(player, mob, data);
        helper.assertTrue(absorbed == 1, "Slime must absorb only the learned breath, not pending resistances: " + absorbed);
        var view = ReincarnatedSkills.snapshot(player, data).stream()
                .filter(row -> row.id().equals("tensura:paralysing_breath")).findFirst().orElseThrow();
        helper.assertTrue(view.mastery() >= 0 && !view.temporary(), "Breath must be learned permanently");
        helper.assertTrue(view.modes().stream().anyMatch(mode -> mode.slottable()), "Breath must be equipable");
        var decoded = com.gabriel.tensuranewworld.data.SkillView.load(view.save());
        helper.assertTrue(decoded.id().equals(view.id()) && decoded.category() == view.category(),
                "The client snapshot must retain the absorbed breath and category");
        var announce = com.gabriel.tensuranewworld.integration.GreatSageAnnouncements.payload(skill);
        helper.assertTrue(ResourceLocation.parse("tensura:paralysing_breath").equals(
                announce.getClass().getMethod("skillId").invoke(announce)), "Voice announcement must identify the acquired skill");
        helper.assertTrue("intrinsic".equals(announce.getClass().getMethod("tier").invoke(announce)),
                "Voice announcement must use the addon's intrinsic tier");
        helper.assertTrue(MonsterSkillAbsorption.absorb(player, mob, data) == 0, "Repeat prey must not reset an existing skill");
        data.setRaceId("tensura:human");
        helper.assertFalse(MonsterSkillAbsorption.canAbsorb(data), "Other races cannot automatically absorb");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void killingCentipedeRunsAbsorptionAndPublishesTheSkill(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "KillAbsorption"));
        var data = player.getData(ModAttachments.PLAYER_DATA);
        data.setRaceId("tensura:slime");
        data.setEvolutionStage("slime");
        helper.assertTrue(com.gabriel.tensuranewworld.integration.ReincarnatedBridge.writeRace(player, "tensura:slime"),
                "Player must have the native slime race");
        var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("tensura:evil_centipede"));
        var mob = (Mob) helper.spawn(type, 1, 2, 1);
        mob.hurt(helper.getLevel().damageSources().playerAttack(player), 10000);
        helper.assertTrue(data.getSkills().contains("tensura:paralysing_breath"), "Real death event must award the skill");
        helper.assertTrue(ReincarnatedSkills.snapshot(player, data).stream().anyMatch(row ->
                row.id().equals("tensura:paralysing_breath") && row.category() ==
                        com.gabriel.tensuranewworld.registry.SkillCategory.INTRINSIC),
                "Raphael must publish the acquired breath in Intrinsics");
        helper.succeed();
    }
}
