package com.gabriel.tensuranewworld.registry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SkillRegistry {
    private static final Map<String, SkillDefinition> SKILLS = new LinkedHashMap<>();

    static {
        register("predator", "Predator", SkillTier.UNIQUE, 250L, "Devour, analyze and isolate targets.");
        register("great_sage", "Great Sage", SkillTier.UNIQUE, 100L, "Accelerated thought and analytical support.");
        register("degenerate", "Degenerate", SkillTier.UNIQUE, 350L, "Separate and synthesize skills.");
        register("gluttony", "Gluttony", SkillTier.UNIQUE, 800L, "Expanded predation and food chain.");
        register("raphael", "Raphael, Lord of Wisdom", SkillTier.ULTIMATE, 1_500L, "Perfect analysis and optimized skill control.");
        register("beelzebuth", "Beelzebuth, Lord of Gluttony", SkillTier.ULTIMATE, 2_000L, "Predation, stomach, isolation and food chain.");
        register("uriel", "Uriel, Lord of Vows", SkillTier.ULTIMATE, 1_750L, "Absolute defense and spatial domination.");
        register("ciel", "Ciel", SkillTier.MANAS, 0L, "A conscious Manas born from Raphael.");
    }

    private SkillRegistry() {
    }

    private static void register(String path, String name, SkillTier tier, long cost, String description) {
        String id = "tensuranewworld:" + path;
        SKILLS.put(id, new SkillDefinition(id, name, tier, cost, description));
    }

    public static Optional<SkillDefinition> get(String id) {
        return Optional.ofNullable(SKILLS.get(normalize(id)));
    }

    public static Map<String, SkillDefinition> all() {
        return Map.copyOf(SKILLS);
    }

    public static String normalize(String id) {
        return id.contains(":") ? id : "tensuranewworld:" + id;
    }
}
