package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.ModAttachments;
import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/** Free native starter skill, also applied to existing characters. */
public final class DefaultGreatSage {
    private DefaultGreatSage() { }

    public static boolean ensure(ServerPlayer player) {
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
        Object learned = ReincarnatedSkills.call(storage, "getLearnedSkills");
        if (!(learned instanceof Collection<?> instances)) return false;
        for (Object instance : instances) {
            String id = String.valueOf(ReincarnatedSkills.call(instance, "getSkillId"));
            // Do not restore a prerequisite consumed by the native Raphael evolution.
            if (id.equals("greatsage:raphael") || id.equals("tensuranewworld:raphael") || id.equals("tensuranewworld:ciel")) return false;
        }
        for (Object instance : instances) {
            if (!"tensura:great_sage".equals(String.valueOf(ReincarnatedSkills.call(instance, "getSkillId")))) continue;
            Object mastery = ReincarnatedSkills.call(instance, "getMastery");
            if (mastery instanceof Number number && number.doubleValue() < 0) {
                ReincarnatedSkills.call(instance, "setMastery", 0.0D);
                ReincarnatedSkills.call(instance, "setRemoveTime", -1);
                return true;
            }
            if (Boolean.TRUE.equals(ReincarnatedSkills.call(instance, "isTemporarySkill"))) {
                ReincarnatedSkills.call(instance, "setRemoveTime", -1);
                return true;
            }
            return false;
        }
        Object registry = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillRegistry");
        if (!(registry instanceof Iterable<?> skills)) return false;
        for (Object skill : skills) {
            if (!"tensura:great_sage".equals(String.valueOf(ReincarnatedSkills.call(skill, "getRegistryName")))) continue;
            Object fresh = ReincarnatedSkills.call(skill, "createDefaultInstance");
            if (fresh == null) return false;
            ReincarnatedSkills.call(fresh, "setMastery", 0.0D);
            ReincarnatedSkills.call(fresh, "setRemoveTime", -1);
            if (!(ReincarnatedSkills.call(fresh, "getOrCreateTag") instanceof CompoundTag tag)) return false;
            tag.putBoolean("NoMagiculeCost", true);
            if (!Boolean.TRUE.equals(ReincarnatedSkills.callStatic("io.github.manasmods.tensura.ability.SkillHelper", "learnSkill", player, fresh))) return false;
            player.getData(ModAttachments.PLAYER_DATA).getSkills().add("tensura:great_sage");
            GreatSageAnnouncements.acquired(player, skill);
            return true;
        }
        return false;
    }
}
