package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import com.gabriel.tensuranewworld.registry.EvolutionPath;
import com.gabriel.tensuranewworld.registry.EvolutionRegistry;
import net.minecraft.server.level.ServerPlayer;

public final class RaceEvolutionManager {
    private RaceEvolutionManager() {
    }

    public static void evaluate(ServerPlayer player, TensuraPlayerData data) {
        ensureStage(data);
        EvolutionPath available = EvolutionRegistry.next(data.getEvolutionStage()).stream()
                .filter(path -> !path.harvestOnly())
                .filter(path -> path.conditions().stream().allMatch(condition -> condition.isMet(data)))
                .findFirst()
                .orElse(null);

        if (available == null) {
            if (!data.getPendingEvolution().isBlank()) {
                EvolutionRegistry.get(data.getPendingEvolution())
                        .filter(path -> path.fromStage().equals(data.getEvolutionStage()))
                        .filter(path -> path.conditions().stream().allMatch(condition -> condition.isMet(data)))
                        .orElseGet(() -> {
                            data.setPendingEvolution("");
                            return null;
                        });
            }
            return;
        }
        if (!available.id().equals(data.getPendingEvolution())) {
            data.setPendingEvolution(available.id());
            VoiceOfTheWorld.notice(player,
                    "Evolution available: " + available.displayName() + ". Open Raphael [R] to inspect it.");
        }
    }

    public static Result requestEvolution(ServerPlayer player, TensuraPlayerData data) {
        ensureStage(data);
        EvolutionPath path = EvolutionRegistry.get(data.getPendingEvolution()).orElse(null);
        if (path == null || path.harvestOnly() || !path.fromStage().equals(data.getEvolutionStage())) {
            evaluate(player, data);
            return new Result(false, "No evolution is ready. Inspect the Evolution tab in Raphael.");
        }
        if (path.conditions().stream().anyMatch(condition -> !condition.isMet(data))) {
            data.setPendingEvolution("");
            return new Result(false, "The world conditions for this evolution are no longer complete.");
        }
        apply(player, data, path);
        evaluate(player, data);
        return new Result(true, "Evolution complete: " + path.displayName() + ".");
    }

    public static boolean applyHarvestEvolution(ServerPlayer player, TensuraPlayerData data) {
        ensureStage(data);
        EvolutionPath path = EvolutionRegistry.next(data.getEvolutionStage()).stream()
                .filter(EvolutionPath::harvestOnly)
                .filter(candidate -> candidate.conditions().stream().allMatch(condition -> condition.isMet(data)))
                .findFirst()
                .orElse(null);
        if (path == null) {
            return false;
        }
        apply(player, data, path);
        return true;
    }

    private static void apply(ServerPlayer player, TensuraPlayerData data, EvolutionPath path) {
        data.setEvolutionStage(path.toStage());
        data.setRaceId(path.toRaceId());
        data.setPendingEvolution("");
        ReincarnatedBridge.writeRace(player, path.toRaceId());
        data.recalculateExistencePoints();
        VoiceOfTheWorld.announce(player,
                player.getGameProfile().getName() + " evolved into " + path.displayName() + ".");
    }

    private static void ensureStage(TensuraPlayerData data) {
        if (data.getEvolutionStage().isBlank()) {
            data.setEvolutionStage(EvolutionRegistry.inferStage(data.getRaceId()));
        }
    }

    public record Result(boolean success, String message) {
    }
}
