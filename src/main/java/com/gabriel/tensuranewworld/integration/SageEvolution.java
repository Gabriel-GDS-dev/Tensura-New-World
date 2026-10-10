package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.gameplay.VoiceOfTheWorld;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.tensura.ability.SkillHelper;
import io.github.manasmods.tensura.storage.TensuraStorages;
import java.util.ArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Milestones are world state, never a random roll or a mere New World skill label. */
public final class SageEvolution {
    private SageEvolution() { }
    public static int eligibleStage(TensuraPlayerData data) {
        if (!data.isTrueDemonLord()) return 1;
        return data.getRaceId().equals("tensura:god_slime") ? 3 : 2;
    }
    public static void evaluate(ServerPlayer player, TensuraPlayerData data) {
        int stage = eligibleStage(data);
        var storage = SkillAPI.getSkillsFrom(player);
        if (stage < 3) {
            storage.forgetSkill(ResourceLocation.parse("tensuranewworld:ciel"), null);
            data.setCielAwakened(false);
        }
        if (stage < 2) {
            storage.forgetSkill(ResourceLocation.parse("tensuranewworld:raphael"), null);
            storage.forgetSkill(ResourceLocation.parse("greatsage:raphael"), null);
            data.setRaphaelAwakened(false);
            DefaultGreatSage.ensure(player);
            return;
        }
        String target = stage == 3 ? "tensuranewworld:ciel" : "tensuranewworld:raphael";
        ensureAddonBridge(player);
        var id = ResourceLocation.parse(target);
        if (storage.getSkill(id).filter(instance -> instance.getMastery() >= 0 && !instance.isTemporarySkill()).isPresent()) {
            data.setRaphaelAwakened(true);
            data.setCielAwakened(stage == 3);
            return;
        }
        var skill = SkillAPI.getSkillRegistry().get(id);
        if (skill == null) return;
        var old = storage.getSkill(ResourceLocation.parse(stage == 3 ? "tensuranewworld:raphael" : "tensura:great_sage"))
                .or(() -> storage.getSkill(ResourceLocation.parse("tensura:great_sage")));
        var fresh = skill.createDefaultInstance();
        fresh.setMastery(old.map(ManasSkillInstance::getMastery).map(value -> Math.max(0, value)).orElse(0.0));
        fresh.setRemoveTime(-1);
        old.ifPresent(instance -> {
            if (instance.getTag() != null) fresh.getOrCreateTag().merge(instance.getTag().copy());
            var cooldowns = new ArrayList<>(instance.getCooldownList());
            while (cooldowns.size() < fresh.getModes()) cooldowns.add(0);
            fresh.setCoolDownList(cooldowns);
        });
        fresh.getOrCreateTag().putBoolean("NoMagiculeCost", true);
        if (!SkillHelper.learnSkill(player, fresh)) return;
        boolean toggled = old.map(ManasSkillInstance::isToggled).orElse(false);
        // Save every preset before native removal clears references to the old skill.
        var ability = TensuraStorages.getAbilityFrom(player);
        var slots = new ArrayList<int[]>();
        old.ifPresent(instance -> {
            for (int preset = 0; preset < ability.getPresets().size(); preset++) {
                for (int slot = 0; slot < ability.getAbilitySlots(preset).size(); slot++) {
                    var equipped = ability.getAbilitySlot(preset, slot);
                    if (equipped.getSkill() == instance.getSkill()) slots.add(new int[]{preset, slot, equipped.getMode()});
                }
            }
        });
        storage.forgetSkill(ResourceLocation.parse("tensura:great_sage"), null);
        if (stage == 3) storage.forgetSkill(ResourceLocation.parse("tensuranewworld:raphael"), null);
        data.setRaphaelAwakened(true);
        data.setCielAwakened(stage == 3);
        data.getSkills().remove("tensura:great_sage");
        data.getSkills().remove("tensuranewworld:great_sage");
        if (stage == 3) data.getSkills().remove("tensuranewworld:raphael");
        data.getSkills().add(target);
        for (var slot : slots) ability.setAbilitySlot(slot[0], slot[1], skill, slot[2]);
        ability.markDirty();
        if (toggled) { fresh.setToggled(true); fresh.onToggleOn(player); }
        fresh.onRespawn(player, false);
        GreatSageAnnouncements.acquired(player, skill);
        VoiceOfTheWorld.notice(player, stage == 3 ? "Raphael recebeu o nome Ciel e despertou como Manas." : "Great Sage evoluiu para Raphael no despertar de Verdadeiro Lorde Demonio.");
    }

    private static void ensureAddonBridge(ServerPlayer player) {
        // Keep the optional addon's G/V interface available after replacing Great Sage.
        if (SkillAPI.getSkillsFrom(player).getSkill(ResourceLocation.parse("greatsage:raphael")).isPresent()) return;
        Object addonSkill = ReincarnatedSkills.call(SkillAPI.getSkillRegistry(), "get", ResourceLocation.parse("greatsage:raphael"));
        if (addonSkill != null) {
            Object bridge = ReincarnatedSkills.call(addonSkill, "createDefaultInstance");
            if (bridge != null && ReincarnatedSkills.call(bridge, "getOrCreateTag") instanceof net.minecraft.nbt.CompoundTag tag) {
                tag.putBoolean("NoMagiculeCost", true);
                ReincarnatedSkills.call(bridge, "setMastery", 0.0D);
                ReincarnatedSkills.callStatic("io.github.manasmods.tensura.ability.SkillHelper", "learnSkill", player, bridge);
            }
        }
    }
}
