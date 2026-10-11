package com.gabriel.tensuranewworld.network;

import com.gabriel.tensuranewworld.client.ClientPlayerState;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class NetworkHandler {
    private NetworkHandler() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(TargetVitalsPayload.TYPE, TargetVitalsPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) ClientPlayerState.acceptTarget(payload);
                }));
        registrar.playToClient(WorldSessionPayload.TYPE, WorldSessionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) com.gabriel.tensuranewworld.client.WorldBattleSelectionClient.accept(payload);
                }));
        registrar.playToServer(BattleSelectionPayload.TYPE, BattleSelectionPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        com.gabriel.tensuranewworld.integration.WorldBattleSelection.update(player, payload.world(), payload.skill(), payload.selected());
                        syncWorldSession(player);
                    }
                }));
        registrar.playToClient(
                SyncPlayerDataPayload.TYPE,
                SyncPlayerDataPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ClientPlayerState.accept(payload.data());
                    }
                }));
        registrar.playToClient(
                SyncMimicFormPayload.TYPE,
                SyncMimicFormPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (FMLEnvironment.dist.isClient()) {
                        ClientPlayerState.acceptMimicForm(payload.playerId(), payload.entityId());
                    }
                }));
    }

    public static void sync(ServerPlayer player, TensuraPlayerData data) {
        var tag = data.save();
        tag.putBoolean("MorphAllowed", com.gabriel.tensuranewworld.integration.MorphAccess.allowed(player));
        tag.put(com.gabriel.tensuranewworld.data.BodyProfile.SNAPSHOT_KEY,
                com.gabriel.tensuranewworld.gameplay.BodyPhysiology.profile(player, data).save());
        var skills = new net.minecraft.nbt.ListTag();
        com.gabriel.tensuranewworld.integration.ReincarnatedSkills.snapshot(player, data)
                .forEach(skill -> skills.add(skill.save()));
        tag.put(com.gabriel.tensuranewworld.integration.ReincarnatedSkills.SNAPSHOT_KEY, skills);
        PacketDistributor.sendToPlayer(player, new SyncPlayerDataPayload(tag));
    }

    public static void syncWorldSession(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new WorldSessionPayload(
                com.gabriel.tensuranewworld.data.WorldIdentity.get(player.getServer()),
                com.gabriel.tensuranewworld.integration.WorldBattleSelection.get(player)));
    }

    public static void syncMimicForm(ServerPlayer player, TensuraPlayerData data) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SyncMimicFormPayload(player.getUUID(), data.getActiveMimicForm()));
    }

    public static void syncMimicForm(ServerPlayer owner, ServerPlayer recipient, TensuraPlayerData data) {
        PacketDistributor.sendToPlayer(recipient, new SyncMimicFormPayload(
                owner.getUUID(), data.getActiveMimicForm()));
    }
}
