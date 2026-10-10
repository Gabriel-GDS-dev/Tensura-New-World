package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import com.gabriel.tensuranewworld.registry.SkillCategory;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

/** Automatic slime predation on defeat, using independent native skill instances. */
public final class MonsterSkillAbsorption {
    private MonsterSkillAbsorption() { }

    public static boolean canAbsorb(TensuraPlayerData data) {
        return RaceRegistry.isSlime(data.getRaceId()) && data.getSkills().stream().anyMatch(id -> {
            String path = id.substring(id.indexOf(':') + 1).toLowerCase(Locale.ROOT);
            return path.equals("predator") || path.equals("gluttony") || path.equals("beelzebuth");
        });
    }

    public static boolean isBasic(SkillCategory category) {
        return category == SkillCategory.INTRINSIC || category == SkillCategory.COMMON
                || category == SkillCategory.EXTRA || category == SkillCategory.RESISTANCE;
    }

    public static int absorb(ServerPlayer player, Mob target, TensuraPlayerData data) {
        if (!canAbsorb(data) || player.isSpectator()) return 0;
        Object source = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", target);
        Object learned = ReincarnatedSkills.call(source, "getLearnedSkills");
        if (!(learned instanceof Collection<?> instances)) return 0;
        int acquired = 0;
        // Learning invokes native callbacks; never iterate a live storage collection.
        for (Object original : List.copyOf(instances)) {
            Object skill = ReincarnatedSkills.call(original, "getSkill");
            Object mastery = ReincarnatedSkills.call(original, "getMastery");
            if (skill == null || !isBasic(SkillCategory.from(
                    String.valueOf(ReincarnatedSkills.call(skill, "getType")), skill.getClass().getName()))
                    || Boolean.TRUE.equals(ReincarnatedSkills.call(original, "isTemporarySkill"))
                    || !(mastery instanceof Number points) || points.doubleValue() < 0
                    || !Double.isFinite(points.doubleValue())) continue;
            // Create a fresh instance: do not inherit the monster's cooldowns, mastery,
            // owner, subskills, or entity references stored in its NBT.
            Object fresh = ReincarnatedSkills.call(skill, "createDefaultInstance");
            if (fresh == null) continue;
            ReincarnatedSkills.call(fresh, "setMastery", 0.0D);
            ReincarnatedSkills.call(fresh, "setRemoveTime", -1);
            // Predation is the acquisition cost. The native one-shot flag avoids
            // buying the skill by permanently reducing the slime's magicule capacity.
            Object tag = ReincarnatedSkills.call(fresh, "getOrCreateTag");
            if (!(tag instanceof net.minecraft.nbt.CompoundTag acquisitionTag)) continue;
            acquisitionTag.putBoolean("NoMagiculeCost", true);
            Object success = ReincarnatedSkills.callStatic("io.github.manasmods.tensura.ability.SkillHelper",
                    "learnSkill", player, fresh);
            if (!Boolean.TRUE.equals(success)) continue;
            Object id = ReincarnatedSkills.call(fresh, "getSkillId");
            if (id != null) data.getSkills().add(id.toString());
            GreatSageAnnouncements.acquired(player, skill);
            acquired++;
        }
        return acquired;
    }
}
