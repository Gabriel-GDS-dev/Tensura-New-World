package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.data.CharacterAttribute;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.network.NetworkHandler;
import com.gabriel.tensuranewworld.registry.RaceDefinition;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public final class PlayerProgression {
    private static final String MODIFIER_NAMESPACE = "tensuranewworld";

    private PlayerProgression() {
    }

    public static void tick(ServerPlayer player) {
        TensuraPlayerData data = player.getData(ModAttachments.PLAYER_DATA);
        HarvestFestivalManager.tick(player, data);
        if (player.tickCount % 5 == 0) com.gabriel.tensuranewworld.gameplay.TargetInspection.sync(player);
        if (player.tickCount % 20 != 0) {
            BodyPhysiology.tick(player, data);
            return;
        }
        ReincarnatedBridge.syncFromBase(player, data);
        com.gabriel.tensuranewworld.integration.MorphAccess.enforce(player);
        if (!com.gabriel.tensuranewworld.integration.MorphAccess.allowed(player)) data.clearMimicForm();
        applyCharacterStats(player, data);
        BodyPhysiology.tick(player, data);
        evaluateWorldConditions(player, data);
        HarvestFestivalManager.evaluate(player, data);
        RaceEvolutionManager.evaluate(player, data);
        com.gabriel.tensuranewworld.integration.SageEvolution.evaluate(player, data);
        com.gabriel.tensuranewworld.integration.SageInitialProgress.completeIfReady(player);
        StoryManager.evaluate(player, data);
        data.recalculateExistencePoints();
        NetworkHandler.sync(player, data);
    }

    private static void applyCharacterStats(ServerPlayer player, TensuraPlayerData data) {
        RaceDefinition race = RaceRegistry.get(data.getRaceId()).orElse(null);
        setModifier(player, Attributes.ATTACK_DAMAGE, "strength",
                stat(data, race, CharacterAttribute.STRENGTH) * 0.5D);
        setModifier(player, Attributes.MAX_HEALTH, "vitality",
                stat(data, race, CharacterAttribute.VITALITY) * 2.0D);
        setModifier(player, Attributes.MOVEMENT_SPEED, "agility",
                stat(data, race, CharacterAttribute.AGILITY) * 0.002D);
        setModifier(player, Attributes.ARMOR, "will",
                stat(data, race, CharacterAttribute.WILL) * 0.1D);

        if (race != null && (race.lineage().equals("merfolk") || race.lineage().equals("lizardman"))) {
            applyTemporaryEffect(player, MobEffects.WATER_BREATHING);
        }
    }

    private static int stat(TensuraPlayerData data, RaceDefinition race, CharacterAttribute attribute) {
        return data.getAttribute(attribute) + (race != null && race.favoredAttribute() == attribute ? 1 : 0);
    }

    private static void applyTemporaryEffect(Player player, Holder<MobEffect> effect) {
        player.addEffect(new MobEffectInstance(effect, 40, 0, true, false, true));
    }

    private static void setModifier(ServerPlayer player, Holder<Attribute> attribute, String path, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODIFIER_NAMESPACE, path);
        instance.removeModifier(id);
        if (amount != 0.0D) {
            instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void evaluateWorldConditions(ServerPlayer player, TensuraPlayerData data) {
        if (BodyPhysiology.profile(player, data).needsFood() && player.getFoodData().getFoodLevel() <= 6) {
            data.setSurvivedCriticalHunger(true);
        }
        if (data.hasSurvivedCriticalHunger() && data.getSkills().stream().anyMatch(PlayerProgression::isHungerSkill)) {
            data.setAbsorbedHungerFactor(true);
        }
        if (data.getBattlesWon() >= 25 && data.getSkills().stream().anyMatch(PlayerProgression::isCombatSkill)) {
            data.setMasteredCombatSkill(true);
        }
    }

    private static boolean isHungerSkill(String id) {
        String value = id.toLowerCase(java.util.Locale.ROOT);
        return value.contains("hunger") || value.contains("glutton") || value.contains("predator")
                || value.contains("beelzebuth");
    }

    private static boolean isCombatSkill(String id) {
        String value = id.toLowerCase(java.util.Locale.ROOT);
        return value.contains("blade") || value.contains("flame") || value.contains("predator")
                || value.contains("glutton") || value.contains("degenerate") || value.contains("warrior");
    }
}
