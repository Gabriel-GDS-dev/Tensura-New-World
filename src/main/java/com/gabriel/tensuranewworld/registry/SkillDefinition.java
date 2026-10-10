package com.gabriel.tensuranewworld.registry;

public record SkillDefinition(String id, String displayName, SkillTier tier, long magiculeCost, String description) {
}
