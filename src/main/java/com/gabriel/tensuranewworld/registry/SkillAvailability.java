package com.gabriel.tensuranewworld.registry;

/** Presentation policy; activation and learning remain owned by Reincarnated. */
public enum SkillAvailability {
    READY("Disponivel", true), PASSIVE("Passiva", true),
    LEARNING("Em aprendizado", false), MODE_LEARNING("Modo em aprendizado", false),
    COOLDOWN("Em recarga", false), ENERGY("Energia insuficiente", false),
    SEALED("Uso impedido", false), UNAVAILABLE("Registro sem ativacao", false);

    public final String label;
    public final boolean vivid;

    SkillAvailability(String label, boolean vivid) {
        this.label = label;
        this.vivid = vivid;
    }

    public static SkillAvailability evaluate(boolean supported, double mastery, boolean modeLearning,
            boolean passive, boolean interacts, int cooldown, boolean ignoresCooldown,
            boolean infiniteEnergy, double magicules, double aura, double mpCost, double apCost) {
        if (!supported) return UNAVAILABLE;
        if (mastery < 0) return LEARNING;
        if (!interacts) return SEALED;
        if (modeLearning) return MODE_LEARNING;
        if (cooldown > 0 && !ignoresCooldown) return COOLDOWN;
        if (!infiniteEnergy && (magicules < mpCost || aura < apCost)) return ENERGY;
        return passive ? PASSIVE : READY;
    }
}
