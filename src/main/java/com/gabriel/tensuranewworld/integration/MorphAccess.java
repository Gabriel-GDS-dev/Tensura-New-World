package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.registry.RaceRegistry;
import java.util.Collection;
import java.util.Set;
import net.minecraft.world.entity.player.Player;

/** Authorizes transformations from live native race and learned skill storage. */
public final class MorphAccess {
    private static final Set<String> HIGHER_PREDATION = Set.of("tensura:gluttony", "tensura:beelzebuth", "tensura:beelzebub", "tensura:azathoth");
    private MorphAccess() { }

    public static boolean allowed(String race, Collection<String> learned) {
        return RaceRegistry.isSlime(race) || learned.stream().anyMatch(HIGHER_PREDATION::contains);
    }

    public static boolean allowed(Player player) {
        String race = ReincarnatedBridge.raceId(player).orElse("");
        if (RaceRegistry.isSlime(race)) return true;
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
        Object learned = ReincarnatedSkills.call(storage, "getLearnedSkills");
        if (learned instanceof Iterable<?> instances) for (Object instance : instances) {
            String id = String.valueOf(ReincarnatedSkills.call(instance, "getSkillId"));
            Object mastery = ReincarnatedSkills.call(instance, "getMastery");
            if (HIGHER_PREDATION.contains(id) && mastery instanceof Number n && n.doubleValue() >= 0) return true;
        }
        return false;
    }

    public static boolean originalBody(Player player, Object variant) {
        return "minecraft:player".equals(String.valueOf(MorphBridge.field(variant, "id")))
                && player.getUUID().equals(MorphBridge.field(MorphBridge.field(variant, "thisVariant"), "playerUUID"));
    }

    public static void enforce(net.minecraft.server.level.ServerPlayer player) {
        if (allowed(player) || !net.neoforged.fml.ModList.get().isLoaded("morph")) return;
        Object api = ReincarnatedSkills.callStatic("me.ichun.mods.morph.api.MorphApi", "getApiImpl");
        Object info = ReincarnatedSkills.call(api, "getMorphInfo", player);
        Object variant = MorphBridge.field(MorphBridge.field(info, "nextState"), "variant");
        if (variant != null && !originalBody(player, variant)) ReincarnatedSkills.call(api, "demorph", player);
    }
}
