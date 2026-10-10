package com.gabriel.tensuranewworld.network;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPlayerDataPayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncPlayerDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TensuraNewWorld.MOD_ID, "sync_player_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerDataPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeNbt(payload.data()),
            buffer -> {
                CompoundTag tag = buffer.readNbt();
                return new SyncPlayerDataPayload(tag == null ? new CompoundTag() : tag);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
