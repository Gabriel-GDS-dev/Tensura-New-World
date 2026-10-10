package com.gabriel.tensuranewworld.registry;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import java.util.List;

public record EvolutionPath(
        String id,
        String fromStage,
        String toStage,
        String toRaceId,
        String displayName,
        String description,
        boolean harvestOnly,
        List<EvolutionCondition> conditions) {

    public EvolutionPath {
        conditions = List.copyOf(conditions);
    }

    public boolean isUnlocked(TensuraPlayerData data) {
        return conditions.stream().allMatch(condition -> condition.isMet(data));
    }
}
