package com.gabriel.tensuranewworld.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TargetVitalsPayload(int entityId, String name, float health, float maxHealth, double magicules, double ep) implements CustomPacketPayload {
    public static final Type<TargetVitalsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("tensuranewworld", "target_vitals"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TargetVitalsPayload> STREAM_CODEC = StreamCodec.of(
            (b, p) -> { b.writeVarInt(p.entityId()); b.writeUtf(p.name(), 256); b.writeFloat(p.health()); b.writeFloat(p.maxHealth()); b.writeDouble(p.magicules()); b.writeDouble(p.ep()); },
            b -> new TargetVitalsPayload(b.readVarInt(), b.readUtf(256), b.readFloat(), b.readFloat(), b.readDouble(), b.readDouble()));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
