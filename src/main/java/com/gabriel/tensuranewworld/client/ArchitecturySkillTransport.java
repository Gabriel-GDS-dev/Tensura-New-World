package com.gabriel.tensuranewworld.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Native Tensura payloads require Architectury's codec/transformer pipeline. */
final class ArchitecturySkillTransport {
    static final String MANAGER = "dev.architectury.networking.NetworkManager";

    private ArchitecturySkillTransport() { }

    static void send(CustomPacketPayload payload) throws ReflectiveOperationException {
        send(payload, Class.forName(MANAGER));
    }

    static void send(CustomPacketPayload payload, Class<?> manager) throws ReflectiveOperationException {
        // The API returns void. Do not use the generic bridge's null return value
        // as a success signal, and never fall back to sending the raw payload.
        manager.getMethod("sendToServer", CustomPacketPayload.class).invoke(null, payload);
    }
}
