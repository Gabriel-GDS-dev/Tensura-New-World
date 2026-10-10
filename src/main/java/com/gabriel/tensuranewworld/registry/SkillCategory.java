package com.gabriel.tensuranewworld.registry;

public enum SkillCategory {
    RESISTANCE("Resistencias", 0xFF70DBAD),
    INTRINSIC("Intrinsecas", 0xFF68CFFF),
    COMMON("Comuns", 0xFF94BFFF),
    EXTRA("Extras", 0xFF4FE8E0),
    UNIQUE("Unicas", 0xFFD4A0FF),
    ULTIMATE("Supremas", 0xFFFFCF76),
    MANAS("Manas", 0xFFFF96C6),
    MAGIC("Magias", 0xFF93A1FF),
    BATTLEWILL("Artes de combate", 0xFFFFAB7F),
    OTHER("Outras", 0xFFC0D8E3);

    public final String label;
    public final int color;

    SkillCategory(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public static SkillCategory from(String type, String className) {
        if (className.endsWith(".CielSkill")) return MANAS;
        if (className.contains(".magic.")) return MAGIC;
        if (className.contains(".battlewill.")) return BATTLEWILL;
        try { return valueOf(type.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException ignored) { return OTHER; }
    }
}
