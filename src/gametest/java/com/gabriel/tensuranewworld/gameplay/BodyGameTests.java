package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.MimicForm;
import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("tensuranewworld")
@PrefixGameTestTemplate(false)
public final class BodyGameTests {
    @GameTest(template = "empty")
    public static void cameraUsesMorphEyesDespiteSlimeSizeModifiers(GameTestHelper helper) throws Exception {
        if (!ModList.get().isLoaded("morph")) { helper.succeed(); return; }
        var player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        var height = player.getAttribute(io.github.manasmods.tensura.registry.attribute.TensuraAttributes.HEIGHT_MULTIPLIER);
        if (height != null) height.setBaseValue(0.2);
        for (String id : java.util.List.of("minecraft:skeleton", "tensura:leech_lizard", "tensura:hover_lizard")) {
            var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse(id));
            var mob = (LivingEntity)type.create(helper.getLevel());
            transform(player, data, mob);
            var camera = com.gabriel.tensuranewworld.client.FormCamera.measure(player, 1);
            helper.assertTrue(camera.eyeHeight() > mob.getBbHeight() * 0.25F, "Camera at feet for " + id);
            helper.assertTrue(camera.eyeHeight() <= mob.getBbHeight(), "Camera above body for " + id);
        }
        clear(player, data);
        if (height != null) height.setBaseValue(1);
        helper.succeed();
    }
    @GameTest(template = "empty")
    public static void horseInheritsStatsAndRestoresDamageRatio(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        double originalSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        float originalMaximum = player.getMaxHealth();
        player.setHealth(originalMaximum / 2);
        LivingEntity horse = EntityType.HORSE.create(helper.getLevel());
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30);
        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.3);
        transform(player, data, horse);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 30) < .001, "Horse max health must be 30, without duplicate modifiers");
        helper.assertTrue(Math.abs(player.getHealth() - 15) < .001, "Changing body must preserve damage ratio");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - .3) < .001, "Horse must inherit captured speed despite Morph cap");
        helper.assertTrue(BodyPhysiology.profile(player, data).needsFood(), "Horse must need food even for a slime player");
        clear(player, data);
        helper.assertTrue(Math.abs(player.getMaxHealth() - originalMaximum) < .001, "Original max health must return");
        helper.assertTrue(Math.abs(player.getHealth() - originalMaximum / 2) < .001, "Returning must not heal damage");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.MOVEMENT_SPEED) - originalSpeed) < .001, "Horse speed must be removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void birdFliesAndTerrestrialBodyCannot(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        transform(player, data, EntityType.PARROT.create(helper.getLevel()));
        helper.assertTrue(BodyPhysiology.profile(player, data).canFly(), "Parrot must be recognized as flying");
        helper.assertTrue(player.getAbilities().mayfly, "Parrot must grant flight on server");
        player.getAbilities().flying = true;
        transform(player, data, EntityType.PIG.create(helper.getLevel()));
        helper.assertFalse(player.getAbilities().mayfly, "Pig cannot retain bird flight");
        helper.assertFalse(player.getAbilities().flying, "Flight must stop after leaving bird");
        helper.assertFalse(BodyPhysiology.profile(player, data).canFly(), "HUD must report terrestrial body");
        clear(player, data);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foodIsSuspendedAndRestoredWithoutRefillingAnimal(GameTestHelper helper) throws Exception {
        ServerPlayer player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        player.getFoodData().setFoodLevel(7);
        player.getFoodData().setSaturation(2);
        BodyPhysiology.tick(player, data);
        helper.assertFalse(BodyPhysiology.profile(player, data).needsFood(), "Slime must not need food");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20, "Slime must be protected against starvation");
        transform(player, data, EntityType.PIG.create(helper.getLevel()));
        helper.assertTrue(player.getFoodData().getFoodLevel() == 7, "Animal must recover saved hunger, not full food");
        player.getFoodData().setFoodLevel(4);
        clear(player, data);
        transform(player, data, EntityType.HORSE.create(helper.getLevel()));
        helper.assertTrue(player.getFoodData().getFoodLevel() == 4, "Repeated transformation must not refill hunger");
        clear(player, data);
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BodyTest"));
        player.setGameMode(GameType.SURVIVAL);
        player.getData(ModAttachments.PLAYER_DATA).setRaceId("tensura:slime");
        return player;
    }

    @GameTest(template = "empty")
    public static void fireImmunityAndFoodlessBodiesFollowCurrentForm(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        BodyPhysiology.tick(player, data);
        helper.assertTrue(damageIsCanceled(player, player.damageSources().starve()), "Foodless slime must block starvation damage");
        transform(player, data, EntityType.BLAZE.create(helper.getLevel()));
        helper.assertTrue(BodyPhysiology.profile(player, data).fireImmune(), "Blaze must carry innate fire immunity");
        helper.assertTrue(damageIsCanceled(player, player.damageSources().inFire()), "Blaze body must block fire damage");
        transform(player, data, EntityType.PIG.create(helper.getLevel()));
        helper.assertFalse(BodyPhysiology.profile(player, data).fireImmune(), "Pig must not keep blaze immunity");
        helper.assertFalse(damageIsCanceled(player, player.damageSources().inFire()), "Previous immunity must end when the body changes");
        transform(player, data, EntityType.IRON_GOLEM.create(helper.getLevel()));
        helper.assertFalse(BodyPhysiology.profile(player, data).needsFood(), "Inorganic golem must not need food");
        clear(player, data);
        helper.succeed();
    }

    private static boolean damageIsCanceled(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source) {
        // NeoForge FakePlayer is always invulnerable; dispatch the actual damage
        // event directly to exercise the installed body and Morph handlers.
        var event = new net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent(player,
                new net.neoforged.neoforge.common.damagesource.DamageContainer(source, 4));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }

    @GameTest(template = "empty")
    public static void attackArmorAndInvestmentsSurviveTheTransformation(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        var bonus = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tensuranewworld", "test_investment");
        player.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                bonus, 4, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        player.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                bonus, 2, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        var zombie = EntityType.ZOMBIE.create(helper.getLevel());
        zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(26);
        zombie.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);
        zombie.getAttribute(Attributes.ARMOR).setBaseValue(4);
        player.setHealth(12);
        transform(player, data, zombie);
        helper.assertTrue(Math.abs(player.getMaxHealth() - 30) < .001, "Body health must retain invested health once");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_DAMAGE) - 9) < .001, "Body attack must retain invested strength once");
        helper.assertTrue(Math.abs(player.getAttributeValue(Attributes.ARMOR) - 4) < .001, "Body armor must be inherited");
        helper.assertFalse(BodyPhysiology.profile(player, data).needsFood(), "Undead body has no biological hunger");
        clear(player, data);
        helper.assertTrue(Math.abs(player.getHealth() - 12) < .001, "Investments must not cause healing on return");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aquaticBodyBreathesAndCreativeFlightIsPreserved(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var data = player.getData(ModAttachments.PLAYER_DATA);
        player.setAirSupply(1);
        transform(player, data, EntityType.COD.create(helper.getLevel()));
        helper.assertTrue(BodyPhysiology.profile(player, data).breathesWater(), "Fish body must breathe water");
        helper.assertTrue(player.getAirSupply() == player.getMaxAirSupply(), "Fish must recover air underwater");
        player.setGameMode(GameType.CREATIVE);
        transform(player, data, EntityType.PARROT.create(helper.getLevel()));
        transform(player, data, EntityType.PIG.create(helper.getLevel()));
        helper.assertTrue(player.getAbilities().mayfly, "Creative flight must not be removed by demorph");
        clear(player, data);
        helper.succeed();
    }

    private static void transform(ServerPlayer player, TensuraPlayerData data, LivingEntity entity) throws Exception {
        var id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        data.recordMimicDefeat(new MimicForm(id, id, 1, entity.getMaxHealth(),
                (float) attribute(entity, Attributes.ATTACK_DAMAGE, 1),
                (float) attribute(entity, Attributes.MOVEMENT_SPEED, .1),
                (float) attribute(entity, Attributes.ARMOR, 0), entity.fireImmune(), entity.canBreatheUnderwater()));
        if (ModList.get().isLoaded("morph")) {
            Object api = api();
            Object variant = api.getClass().getMethod("createVariant", LivingEntity.class).invoke(api, entity);
            setNativeState(player, variant);
        } else data.selectMimicForm(id);
        BodyPhysiology.tick(player, data);
    }

    private static void clear(ServerPlayer player, TensuraPlayerData data) throws Exception {
        if (ModList.get().isLoaded("morph")) {
            Object variant = Class.forName("me.ichun.mods.morph.api.morph.MorphVariant")
                    .getMethod("createPlayerMorph", UUID.class, boolean.class).invoke(null, player.getUUID(), true);
            Object thisVariant = variant.getClass().getField("thisVariant").get(variant);
            thisVariant.getClass().getField("identifier").set(thisVariant, "default_player_state");
            setNativeState(player, variant);
        } else data.clearMimicForm();
        BodyPhysiology.tick(player, data);
    }

    private static void setNativeState(ServerPlayer player, Object variant) throws Exception {
        Class<?> stateClass = Class.forName("me.ichun.mods.morph.api.morph.MorphState");
        Object state = stateClass.getConstructor(variant.getClass(), net.minecraft.world.entity.player.Player.class).newInstance(variant, player);
        Object api = api();
        Object info = api.getClass().getMethod("getMorphInfo", net.minecraft.world.entity.player.Player.class).invoke(api, player);
        info.getClass().getMethod("setNextState", stateClass, int.class).invoke(info, state, 4);
        for (int i = 0; i < 8; i++) {
            info.getClass().getMethod("tick").invoke(info);
            BodyPhysiology.tick(player, player.getData(ModAttachments.PLAYER_DATA));
        }
    }

    private static Object api() throws Exception {
        return Class.forName("me.ichun.mods.morph.api.MorphApi").getMethod("getApiImpl").invoke(null);
    }

    private static double attribute(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double fallback) {
        return entity.getAttribute(attribute) == null ? fallback : entity.getAttributeValue(attribute);
    }
}
