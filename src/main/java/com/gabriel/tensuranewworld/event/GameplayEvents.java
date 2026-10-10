package com.gabriel.tensuranewworld.event;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import com.gabriel.tensuranewworld.data.MimicForm;
import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.gameplay.PlayerProgression;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.integration.MonsterSkillAbsorption;
import com.gabriel.tensuranewworld.network.NetworkHandler;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = TensuraNewWorld.MOD_ID)
public final class GameplayEvents {
    private GameplayEvents() {
    }

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerProgression.tick(player);
        }
    }

    @SubscribeEvent
    public static void beforePlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            com.gabriel.tensuranewworld.gameplay.BodyPhysiology.beforeTick(player,
                    player.getData(ModAttachments.PLAYER_DATA));
        }
    }

    @SubscribeEvent
    public static void onBodyDamage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var body = com.gabriel.tensuranewworld.gameplay.BodyPhysiology.profile(player,
                    player.getData(ModAttachments.PLAYER_DATA));
            if (body.fireImmune() && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
                    || !body.needsFood() && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.STARVE)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TensuraPlayerData data = player.getData(ModAttachments.PLAYER_DATA);
            ReincarnatedBridge.syncFromBase(player, data);
            com.gabriel.tensuranewworld.integration.SageEvolution.evaluate(player, data);
            com.gabriel.tensuranewworld.integration.SageInitialProgress.completeIfReady(player);
            NetworkHandler.syncWorldSession(player);
            NetworkHandler.sync(player, data);
            NetworkHandler.syncMimicForm(player, data);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer tracker
                && event.getTarget() instanceof ServerPlayer tracked) {
            NetworkHandler.syncMimicForm(tracked, tracker,
                    tracked.getData(ModAttachments.PLAYER_DATA));
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer)
            com.gabriel.tensuranewworld.integration.WorldBattleSelection.clone(oldPlayer, newPlayer);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || event.getEntity() == player) {
            return;
        }
        TensuraPlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        // A kill may occur immediately after a skill spends energy. Import the
        // current pool before writing progression, rather than restoring last tick's mana.
        ReincarnatedBridge.syncFromBase(player, data);
        data.addBattleWin();
        long baseEssence = Math.max(1L, Math.round(event.getEntity().getMaxHealth()
                + attributeValue(event.getEntity(), Attributes.ATTACK_DAMAGE) * 2.0D
                + attributeValue(event.getEntity(), Attributes.ARMOR) * 2.0D));
        long essence = Math.max(1L, Math.min(10_000L,
                Math.round(baseEssence * RaceRegistry.essenceMultiplier(data.getRaceId()))));
        data.addEssence(essence);
        ReincarnatedBridge.writeMagicules(player, data.getMagicules(), data.getMaxMagicules());

        if (event.getEntity() instanceof Mob defeated) {
            MonsterSkillAbsorption.absorb(player, defeated, data);
        }

        if (event.getEntity() instanceof Mob mob && (net.neoforged.fml.ModList.get().isLoaded("morph")
                || RaceRegistry.isSlime(data.getRaceId()) && data.getSkills().stream().anyMatch(GameplayEvents::isPredatorSkill))) {
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
            data.recordMimicDefeat(new MimicForm(
                    entityId,
                    mob.getType().getDescription().getString(),
                    1,
                    mob.getMaxHealth(),
                    (float) Math.max(1.0D, attributeValue(mob, Attributes.ATTACK_DAMAGE)),
                    (float) Math.max(0.01D, attributeValue(mob, Attributes.MOVEMENT_SPEED)),
                    (float) Math.max(0.0D, attributeValue(mob, Attributes.ARMOR)),
                    mob.fireImmune(),
                    mob.canBreatheUnderwater()));
        }
        if (event.getEntity() instanceof ServerPlayer || event.getEntity() instanceof Villager) {
            data.setSoulPoints(data.getSoulPoints() + 1);
            ReincarnatedBridge.writeSoulPoints(player, data.getSoulPoints());
        }
        if (event.getEntity().getMaxHealth() >= 100.0F) {
            data.addNotableAct();
        }
        String defeatedType = event.getEntity().getType().toString().toLowerCase(java.util.Locale.ROOT);
        if (player.level().dimension() == Level.NETHER
                && (event.getEntity().getMaxHealth() >= 40.0F
                        || defeatedType.contains("daemon") || defeatedType.contains("demon"))) {
            data.setWonNetherDuel(true);
        }
        data.recalculateExistencePoints();
        NetworkHandler.sync(player, data);
    }

    private static double attributeValue(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute) {
        var instance = entity.getAttribute(attribute);
        return instance == null ? 0.0D : instance.getValue();
    }

    private static boolean isPredatorSkill(String skillId) {
        String normalized = skillId.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("predator") || normalized.contains("glutton")
                || normalized.contains("beelzebuth");
    }
}
