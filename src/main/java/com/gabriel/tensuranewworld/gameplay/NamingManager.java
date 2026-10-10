package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.SubordinateData;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.ReincarnatedBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class NamingManager {
    private NamingManager() {
    }

    public static Result name(ServerPlayer player, Entity target, String chosenName, int tierValue) {
        if (!(target instanceof LivingEntity living) || target == player) {
            return Result.failure("The target must be a living non-player entity.");
        }
        if (player.distanceToSqr(target) > 64.0D) {
            return Result.failure("Move within 8 blocks of the target.");
        }
        if (chosenName.isBlank() || chosenName.length() > 32) {
            return Result.failure("Use a name between 1 and 32 characters.");
        }

        TensuraPlayerData data = player.getData(com.gabriel.tensuranewworld.data.ModAttachments.PLAYER_DATA);
        long targetEp = ReincarnatedBridge.baseExistencePoints(living);
        long cost = ExistencePointCalculator.namingCost(targetEp, tierValue);
        if (data.getMagicules() < cost) {
            return Result.failure("Naming requires " + cost + " magicules; only " + data.getMagicules() + " are available.");
        }

        data.setMagicules(data.getMagicules() - cost);
        living.setCustomName(Component.literal(chosenName));
        living.setCustomNameVisible(true);
        String race = ReincarnatedBridge.raceId(living).orElse(living.getType().toString());
        data.getSubordinates().add(new SubordinateData(living.getUUID(), chosenName, race, targetEp, false));
        data.addNotableAct();
        ReincarnatedBridge.writeMagicules(player, data.getMagicules(), data.getMaxMagicules());
        return Result.success("Named " + chosenName + " for " + cost + " magicules.");
    }

    public record Result(boolean success, String message) {
        public static Result success(String message) { return new Result(true, message); }
        public static Result failure(String message) { return new Result(false, message); }
    }
}
