package com.gabriel.tensuranewworld.gameplay;

import static org.junit.jupiter.api.Assertions.*;
import com.gabriel.tensuranewworld.data.BodyProfile;
import org.junit.jupiter.api.Test;

class BodyRulesTest {
    @Test void digestiveRulesFollowLineage() {
        assertFalse(BodyRules.raceNeedsFood("tensura:slime"));
        assertFalse(BodyRules.raceNeedsFood("tensura:lesser_daemon"));
        assertFalse(BodyRules.raceNeedsFood("tensura:wight"));
        assertTrue(BodyRules.raceNeedsFood("tensura:human"));
        assertTrue(BodyRules.raceNeedsFood("tensura:lizardman"));
        assertTrue(BodyRules.raceNeedsFood("unknown:animal"));
    }
    @Test void nativeMorphAndOurCorrectionDoNotStackTheSameStats() {
        // Native Morph already gives this body 30 health: our correction is zero.
        assertEquals(0, BodyRules.correction(30, 20, 10));
        // The native capped speed is .1; a .225 horse needs the missing .125.
        assertEquals(.125, BodyRules.correction(.225, .1, 0), 1E-9);
        assertEquals(0, BodyRules.correction(Double.NaN, 20, 0));
    }
    @Test void changingBodiesKeepsDamageRatioAndCannotResurrect() {
        assertEquals(5, BodyRules.rescaledHealth(10, 20, 10));
        assertEquals(10, BodyRules.rescaledHealth(5, 10, 20));
        assertEquals(0, BodyRules.rescaledHealth(0, 20, 40));
        assertEquals(10, BodyRules.rescaledHealth(30, 20, 10));
    }
    @Test void hudBarsHandleEmptyPoolsAndLargeMagiculeValues() {
        assertEquals(176, BodyRules.barPixels(Long.MAX_VALUE, Long.MAX_VALUE, 176));
        assertEquals(88, BodyRules.barPixels(50, 100, 176));
        assertEquals(0, BodyRules.barPixels(-10, 100, 176));
        assertEquals(176, BodyRules.barPixels(200, 100, 176));
        assertEquals(0, BodyRules.barPixels(10, 0, 176));
        assertEquals(0, BodyRules.barPixels(Double.NaN, 100, 176));
    }
    @Test void transientBodySnapshotIncludesEverySurvivalCapability() {
        var body = new BodyProfile("minecraft:parrot", "Papagaio", true, true, false, false, true);
        assertEquals(body, BodyProfile.load(body.save()));
    }
}
