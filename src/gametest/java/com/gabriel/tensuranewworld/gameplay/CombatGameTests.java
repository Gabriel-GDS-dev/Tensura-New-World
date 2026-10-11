package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.integration.MorphAccess;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.network.TargetVitalsPayload;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import io.github.manasmods.manascore.race.api.RaceAPI;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class CombatGameTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void realMorphRejectsNonSlimesAndUnlocksOnlyLearnedGluttony(GameTestHelper helper) throws Exception {
        var registry = helper.getLevel().registryAccess().registryOrThrow(RaceAPI.getRaceRegistryKey());
        Object api = ReincarnatedSkills.callStatic("me.ichun.mods.morph.api.MorphApi", "getApiImpl");
        var mob = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
        Object variant = ReincarnatedSkills.call(api, "createVariant", mob);
        for (var id : registry.keySet()) {
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "MorphGate"));
            helper.assertTrue(ReincarnatedBridge.writeRace(player, id.toString()), "Race assignment: " + id);
            boolean slime = com.gabriel.tensuranewworld.registry.RaceRegistry.isSlime(id.toString());
            helper.assertTrue(MorphAccess.allowed(player) == slime, "Native rule: " + id);
            if (api != null && !slime) {
                helper.assertFalse(Boolean.TRUE.equals(ReincarnatedSkills.call(api, "acquireMorph", player, variant)), "Direct acquisition bypass: " + id);
                helper.assertFalse(Boolean.TRUE.equals(ReincarnatedSkills.call(api, "morphTo", player, variant)), "Direct transformation bypass: " + id);
            }
        }
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "GluttonyGate"));
        ReincarnatedBridge.writeRace(player, "tensura:human");
        Object supplier = Class.forName("io.github.manasmods.tensura.registry.skill.UniqueSkills").getField("GLUTTONY").get(null);
        Object skill = ((java.util.function.Supplier<?>) supplier).get();
        Object instance = ReincarnatedSkills.call(skill, "createDefaultInstance");
        ReincarnatedSkills.call(instance, "setMastery", -1.0D);
        ReincarnatedSkills.callStatic("io.github.manasmods.tensura.ability.SkillHelper", "learnSkill", player, instance);
        helper.assertFalse(MorphAccess.allowed(player), "Pending Gluttony cannot unlock Morph");
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
        Object found = ReincarnatedSkills.call(storage, "getSkill", skill);
        Object learned = ((java.util.Optional<?>) found).orElseThrow();
        ReincarnatedSkills.call(learned, "setMastery", 0.0D);
        helper.assertTrue(MorphAccess.allowed(player), "Learned native Gluttony unlocks Human");
        if (api != null) {
            helper.assertTrue(Boolean.TRUE.equals(ReincarnatedSkills.call(api, "acquireMorph", player, variant)), "Gula must allow direct acquisition");
            helper.assertTrue(Boolean.TRUE.equals(ReincarnatedSkills.call(api, "morphTo", player, variant)), "Gula must allow direct transformation");
            ReincarnatedSkills.call(storage, "forgetSkill", net.minecraft.resources.ResourceLocation.parse("tensura:gluttony"));
            MorphAccess.enforce(player);
            helper.assertFalse(MorphAccess.allowed(player), "Removing Gula revokes permission");
            Object info = ReincarnatedSkills.call(api, "getMorphInfo", player);
            Object next = com.gabriel.tensuranewworld.integration.MorphBridge.field(info, "nextState");
            Object nextVariant = com.gabriel.tensuranewworld.integration.MorphBridge.field(next, "variant");
            helper.assertTrue(MorphAccess.originalBody(player, nextVariant), "Revocation must return to original body");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void targetInspectionRespectsWallsAndAim(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "TargetAim"));
        var base = helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1));
        for (int z = 0; z <= 5; z++) for (int y = 0; y <= 2; y++)
            helper.getLevel().setBlock(base.offset(0, y, z), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        player.moveTo(base.getX() + .5, base.getY(), base.getZ() + .5, 0, 0);
        var mob = net.minecraft.world.entity.EntityType.ZOMBIE.create(helper.getLevel());
        mob.setPos(player.getX(), player.getY(), player.getZ() + 4); mob.setNoAi(true);
        helper.getLevel().addFreshEntity(mob);
        helper.assertTrue(TargetInspection.target(player) == mob, "Aimed target must be selected at range");
        var wall = net.minecraft.core.BlockPos.containing(player.getEyePosition().add(0, 0, 2));
        helper.getLevel().setBlock(wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(TargetInspection.target(player) == null, "Wall must hide target vitals");
        helper.getLevel().setBlock(wall, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        player.setYRot(90);
        helper.assertTrue(TargetInspection.target(player) == null, "Turning away must clear target");
        mob.discard(); helper.succeed();
    }

    @GameTest(template = "empty")
    public static void targetVitalsCarryActualHealthAndMagicules(GameTestHelper helper) {
        var mob = net.minecraft.world.entity.EntityType.COW.create(helper.getLevel());
        mob.setHealth(7);
        ReincarnatedBridge.writeMagicules(mob, 1234, 1234);
        var view = TargetInspection.inspect(mob);
        helper.assertTrue(view.health() == 7 && view.maxHealth() == mob.getMaxHealth(), "Target health must be live");
        helper.assertTrue(view.magicules() == 1234, "Magicules must come from the native server storage");
        var b = new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            TargetVitalsPayload.STREAM_CODEC.encode(b, view);
            helper.assertTrue(TargetVitalsPayload.STREAM_CODEC.decode(b).equals(view), "HUD payload must roundtrip");
        } finally { b.release(); }
        helper.succeed();
    }
}
