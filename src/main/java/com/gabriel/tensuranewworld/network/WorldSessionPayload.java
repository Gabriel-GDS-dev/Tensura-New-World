package com.gabriel.tensuranewworld.network;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WorldSessionPayload(UUID world, List<String> skills) implements CustomPacketPayload {
    public static final Type<WorldSessionPayload> TYPE = new Type<>(ResourceLocation.parse("tensuranewworld:world_session"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WorldSessionPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> { buffer.writeUUID(payload.world()); buffer.writeCollection(payload.skills(), (out, id) -> out.writeUtf(id, 256)); },
            buffer -> {
                UUID world = buffer.readUUID();
                int count = buffer.readVarInt();
                if (count < 0 || count > 1024) throw new io.netty.handler.codec.DecoderException("Invalid battle selection size");
                var skills = new java.util.ArrayList<String>(count);
                for (int index = 0; index < count; index++) skills.add(buffer.readUtf(256));
                return new WorldSessionPayload(world, List.copyOf(skills));
            });
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
