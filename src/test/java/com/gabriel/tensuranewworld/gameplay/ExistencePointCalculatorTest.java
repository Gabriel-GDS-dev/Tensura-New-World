package com.gabriel.tensuranewworld.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.gabriel.tensuranewworld.registry.SkillTier;
import java.util.List;
import org.junit.jupiter.api.Test;

final class ExistencePointCalculatorTest {
    @Test
    void followsTheDocumentedEpFormula() {
        assertEquals(31_400L, ExistencePointCalculator.calculate(
                1_000L,
                400L,
                1.0D,
                List.of(SkillTier.UNIQUE, SkillTier.UNIQUE)));
    }

    @Test
    void namingCostUsesDoubleEpPlusTier() {
        assertEquals(70_000L, ExistencePointCalculator.namingCost(20_000L, 3));
    }
}
