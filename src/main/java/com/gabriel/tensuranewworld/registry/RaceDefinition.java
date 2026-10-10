package com.gabriel.tensuranewworld.registry;

import com.gabriel.tensuranewworld.data.CharacterAttribute;

public record RaceDefinition(
        String id,
        String lineage,
        String displayName,
        double physicalMultiplier,
        double essenceMultiplier,
        long essencePerGrowthPoint,
        CharacterAttribute favoredAttribute,
        String passive) {
}
