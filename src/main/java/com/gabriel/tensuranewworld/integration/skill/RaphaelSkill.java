package com.gabriel.tensuranewworld.integration.skill;

import com.gabriel.tensuranewworld.data.ModAttachments;
import com.gabriel.tensuranewworld.integration.SageEvolution;
import io.github.manasmods.manascore.skill.api.ManasSkillInstance;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.tensura.ability.skill.unique.GreatSageSkill;
import io.github.manasmods.tensura.util.EnergyHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** New World adaptation: retains the native sage modes and adds costly optimization. */
public class RaphaelSkill extends GreatSageSkill {
    protected boolean ciel() { return false; }
    @Override public SkillType getType() { return SkillType.ULTIMATE; }
    @Override public MutableComponent getName() { return Component.literal(ciel() ? "Ciel" : "Raphael, Lord of Wisdom"); }
    @Override public ResourceLocation getSkillIcon() { return ResourceLocation.parse("tensura:textures/skill/unique/great_sage.png"); }
    @Override public MutableComponent getSkillDescription() {
        return Component.literal("Avaliacao, analise e refinamento nativos. Otimizacao reduz recargas de outras habilidades aprendidas em "
                + (ciel() ? "50% por 15000 magicules; recarga de 60 segundos." : "25% por 5000 magicules; recarga de 60 segundos.")
                + " Uma adaptacao de gameplay do New World, sem conceder automaticamente outras Supremas.");
    }
    @Override public double getDefaultAcquiringMagiculeCost() { return 0; }
    @Override public int getModes(ManasSkillInstance instance) { return 4; }
    @Override public int nextMode(LivingEntity entity, ManasSkillInstance instance, int mode, boolean reverse) {
        return Math.floorMod(mode + (reverse ? -1 : 1), 4);
    }
    @Override public String getModeId(ManasSkillInstance instance, int mode) {
        return mode == 3 ? "new_world.optimization" : super.getModeId(instance, mode);
    }
    @Override public Component getModeName(ManasSkillInstance instance, int mode) {
        return mode == 3 ? Component.literal("Otimizacao de recargas") : super.getModeName(instance, mode);
    }
    @Override public double getMagiculeCost(LivingEntity entity, ManasSkillInstance instance, int mode) {
        return mode == 3 ? (ciel() ? 15000 : 5000) : super.getMagiculeCost(entity, instance, mode);
    }
    @Override public boolean canActivateSkill(ManasSkillInstance instance, LivingEntity entity, int mode) {
        var data = entity.getData(ModAttachments.PLAYER_DATA);
        return (entity.level().isClientSide() || SageEvolution.eligibleStage(data) >= (ciel() ? 3 : 2)) && super.canActivateSkill(instance, entity, mode);
    }
    @Override public boolean canBeToggled(ManasSkillInstance instance, LivingEntity entity) {
        var data = entity.getData(ModAttachments.PLAYER_DATA);
        return (entity.level().isClientSide() || SageEvolution.eligibleStage(data) >= (ciel() ? 3 : 2)) && super.canBeToggled(instance, entity);
    }
    @Override public void onPressed(ManasSkillInstance instance, LivingEntity entity, int keyNumber, int mode) {
        if (mode != 3) { super.onPressed(instance, entity, keyNumber, mode); return; }
        if (entity.level().isClientSide()) return;
        if (!canActivateSkill(instance, entity, mode) || instance.onCoolDown(mode)
                || EnergyHelper.isOutOfEnergy(entity, instance, mode)) return;
        for (var other : SkillAPI.getSkillsFrom(entity).getLearnedSkills()) {
            if (other == instance || other.getSkill() instanceof RaphaelSkill || other.getMastery() < 0) continue;
            for (int index = 0; index < other.getModes(); index++) {
                int remaining = other.getCoolDown(index);
                if (remaining > 0) other.setCoolDown((int) Math.ceil(remaining * (ciel() ? 0.5 : 0.75)), index);
            }
        }
        instance.setCoolDown(1200, mode);
    }
}
