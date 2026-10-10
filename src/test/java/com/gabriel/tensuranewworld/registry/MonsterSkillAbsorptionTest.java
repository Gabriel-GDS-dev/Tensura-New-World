package com.gabriel.tensuranewworld.registry;

import com.gabriel.tensuranewworld.integration.MonsterSkillAbsorption;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonsterSkillAbsorptionTest {
    @Test void absorbsBasicAbilitiesWithoutStealingHigherPowers() {
        for (var category : SkillCategory.values()) {
            assertEquals(java.util.Set.of(SkillCategory.INTRINSIC, SkillCategory.COMMON,
                    SkillCategory.EXTRA, SkillCategory.RESISTANCE).contains(category),
                    MonsterSkillAbsorption.isBasic(category), category.name());
        }
    }
}
