package com.gabriel.tensuranewworld.network;

import com.gabriel.tensuranewworld.TensuraNewWorld;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncMimicFormPayload(UUID playerId, String entityId) implements CustomPacketPayload {
    public static final Type<SyncMimicFormPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(TensuraNewWorld.MOD_ID, "sync_mimic_form"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMimicFormPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeUUID(payload.playerId());
                buffer.writeUtf(payload.entityId(), 256);
            },
            buffer -> new SyncMimicFormPayload(buffer.readUUID(), buffer.readUtf(256)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
