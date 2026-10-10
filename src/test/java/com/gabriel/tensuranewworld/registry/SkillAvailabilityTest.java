package com.gabriel.tensuranewworld.registry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SkillAvailabilityTest {
    private SkillAvailability state(double mastery, boolean modeLearning, boolean passive, boolean interacts,
            int cooldown, boolean ignoresCooldown, boolean creative, double mp, double ap) {
        return SkillAvailability.evaluate(true, mastery, modeLearning, passive, interacts, cooldown,
                ignoresCooldown, creative, mp, ap, 250, 50);
    }

    @Test void learnedActiveSkillIsVividWhenBothEnergyPoolsAreEnough() {
        assertEquals(SkillAvailability.READY, state(0, false, false, true, 0, false, false, 250, 50));
        assertTrue(SkillAvailability.READY.vivid);
    }
    @Test void learningAndLockedModesStayGrayEvenWithPlentyOfEnergy() {
        assertEquals(SkillAvailability.LEARNING, state(-20, false, false, true, 0, false, false, 10000, 10000));
        assertEquals(SkillAvailability.MODE_LEARNING, state(20, true, false, true, 0, false, false, 10000, 10000));
        assertFalse(SkillAvailability.LEARNING.vivid);
        assertFalse(SkillAvailability.MODE_LEARNING.vivid);
    }
    @Test void insufficientAuraAlsoPreventsUse() {
        assertEquals(SkillAvailability.ENERGY, state(0, false, false, true, 0, false, false, 249, 50));
        assertEquals(SkillAvailability.ENERGY, state(0, false, false, true, 0, false, false, 250, 49));
        assertFalse(SkillAvailability.ENERGY.vivid);
    }
    @Test void cooldownCanBeIgnoredOnlyWhenNativeApiAllowsIt() {
        assertEquals(SkillAvailability.COOLDOWN, state(0, false, false, true, 100, false, false, 1000, 1000));
        assertEquals(SkillAvailability.READY, state(0, false, false, true, 100, true, false, 1000, 1000));
    }
    @Test void creativeBypassesEnergyButNotStatusOrLearning() {
        assertEquals(SkillAvailability.READY, state(0, false, false, true, 0, false, true, 0, 0));
        assertEquals(SkillAvailability.SEALED, state(0, false, false, false, 0, false, true, 0, 0));
        assertEquals(SkillAvailability.LEARNING, state(-1, false, false, true, 0, false, true, 0, 0));
    }
    @Test void passiveSkillsAreNotMistakenForLockedActiveSkills() {
        assertEquals(SkillAvailability.PASSIVE, state(0, false, true, true, 0, false, false, 1000, 1000));
        assertTrue(SkillAvailability.PASSIVE.vivid);
    }
    @Test void unsupportedProgressionMarkersNeverShowAsUsable() {
        assertEquals(SkillAvailability.UNAVAILABLE, SkillAvailability.evaluate(false, 999, false, false,
                true, 0, false, true, 99999, 99999, 0, 0));
        assertFalse(SkillAvailability.UNAVAILABLE.vivid);
    }
    @Test void classifiesAllNativeFamiliesAndAddonTypes() {
        for (SkillCategory category : SkillCategory.values()) assertEquals(category, SkillCategory.from(category.name(), ""));
        assertEquals(SkillCategory.MAGIC, SkillCategory.from("SPIRITUAL", "mod.ability.magic.spiritual.Fire"));
        assertEquals(SkillCategory.BATTLEWILL, SkillCategory.from("null", "mod.ability.battlewill.Strike"));
        assertEquals(SkillCategory.OTHER, SkillCategory.from("unknown", "mod.NewAbility"));
    }
}
