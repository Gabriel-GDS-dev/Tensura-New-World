package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import com.gabriel.tensuranewworld.registry.SkillRegistry;
import com.gabriel.tensuranewworld.registry.SkillTier;
import java.util.Collection;

public final class ExistencePointCalculator {
    private ExistencePointCalculator() {
    }

    public static long calculate(TensuraPlayerData data) {
        Collection<SkillTier> tiers = data.getSkills().stream()
                .map(SkillRegistry::get)
                .flatMap(java.util.Optional::stream)
                .map(skill -> skill.tier())
                .toList();
        return calculate(
                data.getMaxMagicules(),
                data.getBasePhysicalPower(),
                RaceRegistry.physicalMultiplier(data.getRaceId()),
                tiers);
    }

    public static long calculate(long maxMagicules, long basePhysicalPower, double raceMultiplier, Collection<SkillTier> tiers) {
        long physical = Math.round(Math.max(0L, basePhysicalPower) * Math.max(0.0D, raceMultiplier));
        long skillPower = tiers.stream().mapToLong(SkillTier::existenceContribution).sum();
        return saturatedAdd(saturatedAdd(Math.max(0L, maxMagicules), physical), skillPower);
    }

    public static long namingCost(long targetBaseEp, int tierValue) {
        return Math.max(0L, targetBaseEp) * 2L + Math.max(1, tierValue) * 10_000L;
    }

    private static long saturatedAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
