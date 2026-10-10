package com.gabriel.tensuranewworld.gameplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class VoiceOfTheWorld {
    private static final String PREFIX = "[Voice of the World] ";

    private VoiceOfTheWorld() {
    }

    public static void notice(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(PREFIX).withStyle(ChatFormatting.AQUA)
                .append(Component.literal(message).withStyle(ChatFormatting.WHITE)));
    }

    public static void announce(ServerPlayer source, String message) {
        source.server.getPlayerList().broadcastSystemMessage(
                Component.literal(PREFIX).withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(message).withStyle(ChatFormatting.YELLOW)),
                false);
    }
}
