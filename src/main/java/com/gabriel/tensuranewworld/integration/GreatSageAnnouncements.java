package com.gabriel.tensuranewworld.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/** Uses the addon's recorded voice and subtitle queue for confirmed predation. */
public final class GreatSageAnnouncements {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GreatSageAnnouncements.class);
    private static final String PAYLOAD = "dev.dedek.greatsage.network.SkillAnnouncePayload";

    private GreatSageAnnouncements() { }

    public static CustomPacketPayload payload(Object skill) throws ReflectiveOperationException {
        Object id = ReincarnatedSkills.call(skill, "getRegistryName");
        Object type = ReincarnatedSkills.call(skill, "getType");
        Object name = ReincarnatedSkills.call(skill, "getName");
        if (!(id instanceof ResourceLocation location) || !(name instanceof Component component) || type == null) {
            throw new IllegalArgumentException("Missing native skill identity");
        }
        return (CustomPacketPayload) Class.forName(PAYLOAD)
                .getConstructor(ResourceLocation.class, String.class, Component.class)
                .newInstance(location, type.toString().toLowerCase(java.util.Locale.ROOT), component);
    }

    public static void acquired(ServerPlayer player, Object skill) {
        Object name = ReincarnatedSkills.call(skill, "getName");
        Component displayName = name instanceof Component component ? component : Component.literal("?");
        Component message = Component.translatable("message.tensuranewworld.sage.absorbed", displayName);
        player.displayClientMessage(message, false);
        player.displayClientMessage(message, true);
        try {
            Class.forName(PAYLOAD);
            // The addon's normal unlock listener can suppress announcements during
            // login grace or ceremonies. Predation is explicit, confirmed acquisition.
            // Its client queue deduplicates identical skill IDs in the same batch.
            PacketDistributor.sendToPlayer(player, payload(skill));
        } catch (ClassNotFoundException ignored) {
            // The translated text remains available when the optional addon is absent.
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            LOGGER.warn("Great Sage could not announce an absorbed skill", error);
        }
    }
}
