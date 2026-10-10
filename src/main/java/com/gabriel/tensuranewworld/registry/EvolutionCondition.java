package com.gabriel.tensuranewworld.registry;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;

public record EvolutionCondition(Type type, long value, String detail) {
    public static EvolutionCondition ep(long value) { return new EvolutionCondition(Type.EP, value, ""); }
    public static EvolutionCondition battles(int value) { return new EvolutionCondition(Type.BATTLES, value, ""); }
    public static EvolutionCondition title(String title) { return new EvolutionCondition(Type.TITLE, 0L, title); }
    public static EvolutionCondition flag(Type type) { return new EvolutionCondition(type, 0L, ""); }

    public boolean isMet(TensuraPlayerData data) {
        return switch (type) {
            case EP -> data.getExistencePoints() >= value;
            case RECEIVED_NAME -> data.hasReceivedName();
            case BATTLES -> data.getBattlesWon() >= value;
            case TITLE -> data.getTitles().contains(detail);
            case TRUE_DEMON_LORD -> data.isTrueDemonLord();
            case COMBAT_MASTERY -> data.hasMasteredCombatSkill();
            case ELEMENTAL_BLESSING -> data.hasElementalBlessing();
            case HUNGER_CRITICAL -> data.hasSurvivedCriticalHunger();
            case HUNGER_FACTOR -> data.hasAbsorbedHungerFactor();
            case SOUL_CONTRACT -> data.hasSoulContract();
            case NETHER_DUEL -> data.hasWonNetherDuel();
            case HERO_EGG -> data.hasHeroEgg();
            case TRUE_HERO -> data.isTrueHero();
        };
    }

    public String label(TensuraPlayerData data) {
        return switch (type) {
            case EP -> "Existence Points: " + format(data.getExistencePoints()) + " / " + format(value);
            case RECEIVED_NAME -> "Receive a true name from another being";
            case BATTLES -> "Survive battles: " + data.getBattlesWon() + " / " + value;
            case TITLE -> "Obtain title: " + detail;
            case TRUE_DEMON_LORD -> "Complete the Harvest Festival";
            case COMBAT_MASTERY -> "Master a combat skill in real battles";
            case ELEMENTAL_BLESSING -> "Receive an elemental altar blessing";
            case HUNGER_CRITICAL -> "Survive critical hunger";
            case HUNGER_FACTOR -> "Absorb the Hunger factor";
            case SOUL_CONTRACT -> "Form a soul contract with a human";
            case NETHER_DUEL -> "Win a demonic duel in the Nether";
            case HERO_EGG -> "Awaken the Hero Egg";
            case TRUE_HERO -> "Awaken as a True Hero";
        };
    }

    private static String format(long value) {
        if (value >= 1_000_000L) return String.format(java.util.Locale.ROOT, "%.1fM", value / 1_000_000.0D);
        if (value >= 1_000L) return String.format(java.util.Locale.ROOT, "%.0fK", value / 1_000.0D);
        return Long.toString(value);
    }

    public enum Type {
        EP,
        RECEIVED_NAME,
        BATTLES,
        TITLE,
        TRUE_DEMON_LORD,
        COMBAT_MASTERY,
        ELEMENTAL_BLESSING,
        HUNGER_CRITICAL,
        HUNGER_FACTOR,
        SOUL_CONTRACT,
        NETHER_DUEL,
        HERO_EGG,
        TRUE_HERO
    }
}
