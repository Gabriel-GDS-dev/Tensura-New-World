package com.gabriel.tensuranewworld.integration.skill;

import io.github.manasmods.manascore.skill.impl.SkillRegistry;
import net.minecraft.resources.ResourceLocation;

public final class SageSkills {
    private SageSkills() { }
    public static void register() {
        SkillRegistry.SKILLS.register(ResourceLocation.parse("tensuranewworld:raphael"), RaphaelSkill::new);
        SkillRegistry.SKILLS.register(ResourceLocation.parse("tensuranewworld:ciel"), CielSkill::new);
    }
}
