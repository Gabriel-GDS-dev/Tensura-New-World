package com.gabriel.tensuranewworld.registry;

public enum SkillTier {
    COMMON(1, 500L),
    EXTRA(2, 2_500L),
    UNIQUE(3, 15_000L),
    ULTIMATE(4, 100_000L),
    MANAS(5, 500_000L);

    private final int namingValue;
    private final long existenceContribution;

    SkillTier(int namingValue, long existenceContribution) {
        this.namingValue = namingValue;
        this.existenceContribution = existenceContribution;
    }

    public int namingValue() { return namingValue; }
    public long existenceContribution() { return existenceContribution; }
}
