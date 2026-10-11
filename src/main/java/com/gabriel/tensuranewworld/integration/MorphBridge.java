package com.gabriel.tensuranewworld.integration;

import java.lang.reflect.Field;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/** Optional Morph 0.1.4 API; never sends native Morph packets itself. */
public final class MorphBridge {
    public record State(LivingEntity entity, CompoundTag attributes, boolean transitioning, boolean nativeFlight) { }
    private MorphBridge() { }

    public static State current(Player player) {
        if (!ModList.get().isLoaded("morph")) return null;
        Object api = ReincarnatedSkills.callStatic("me.ichun.mods.morph.api.MorphApi", "getApiImpl");
        Object info = ReincarnatedSkills.call(api, "getMorphInfo", player);
        if (!Boolean.TRUE.equals(ReincarnatedSkills.call(info, "isMorphed"))) return null;
        Object progress = ReincarnatedSkills.call(info, "getMorphProgress", 0.0F);
        // The render interpolation reaches 1 before the server has removed the
        // previous modifiers. Wait for Morph's actual state handoff.
        if (field(info, "prevState") != null || !(progress instanceof Number number) || number.floatValue() < 1) {
            return new State(null, new CompoundTag(), true, false);
        }
        Object active = ReincarnatedSkills.call(info, "getActiveMorphEntity");
        if (!(active instanceof LivingEntity entity) || entity instanceof Player) return null;
        Object state = field(info, "nextState");
        Object tag = field(field(state, "variant"), "nbtMorph");
        boolean flight = false;
        if (field(state, "traits") instanceof Iterable<?> traits) {
            for (Object trait : traits) {
                if ("abilityFlight".equals(field(trait, "type"))) { flight = true; break; }
            }
        }
        return new State(entity, tag instanceof CompoundTag nbt ? nbt : new CompoundTag(), false, flight);
    }

    public static Object field(Object target, String name) {
        if (target == null) return null;
        try { Field field = target.getClass().getField(name); return field.get(target); }
        catch (ReflectiveOperationException | RuntimeException ignored) { return null; }
    }
}
