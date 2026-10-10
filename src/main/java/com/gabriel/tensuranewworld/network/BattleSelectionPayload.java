package com.gabriel.tensuranewworld.network;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BattleSelectionPayload(UUID world, String skill, boolean selected) implements CustomPacketPayload {
    public static final Type<BattleSelectionPayload> TYPE = new Type<>(ResourceLocation.parse("tensuranewworld:battle_selection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BattleSelectionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeUUID(payload.world()); buffer.writeUtf(payload.skill(), 256); buffer.writeBoolean(payload.selected()); },
            buffer -> new BattleSelectionPayload(buffer.readUUID(), buffer.readUtf(256), buffer.readBoolean()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
