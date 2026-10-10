package com.gabriel.tensuranewworld.integration;

import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

/** Starter kits are a baseline, not earned sage experience. */
public final class SageInitialProgress {
    private static final String READY = "tnw_sage_initialized";
    private SageInitialProgress() { }
    private static CompoundTag persisted(ServerPlayer player) {
        var data = player.getPersistentData();
        if (!data.contains("PlayerPersisted")) data.put("PlayerPersisted", new CompoundTag());
        return data.getCompound("PlayerPersisted");
    }
    public static boolean initializing(ServerPlayer player) {
        return !persisted(player).getBoolean(READY)
                || player.containerMenu.getClass().getName().equals("io.github.manasmods.tensura.menu.ReincarnationMenu");
    }
    public static void completeIfReady(ServerPlayer player) {
        if (!player.containerMenu.getClass().getName().equals("io.github.manasmods.tensura.menu.ReincarnationMenu")
                && ReincarnatedBridge.raceId(player).isPresent()) persisted(player).putBoolean(READY, true);
    }
    public static void recordStarterSkills(ServerPlayer player, List<String> ids) {
        var data = persisted(player);
        if (!data.contains("greatsage_progress")) data.put("greatsage_progress", new CompoundTag());
        var root = data.getCompound("greatsage_progress");
        var seen = new LinkedHashSet<String>();
        var list = root.getList("seen_skills", 8);
        for (int index = 0; index < list.size(); index++) seen.add(list.getString(index));
        for (String id : ids) if (id != null && !id.isBlank()) seen.add(id.toLowerCase(java.util.Locale.ROOT));
        var updated = new ListTag();
        seen.forEach(id -> updated.add(StringTag.valueOf(id)));
        root.put("seen_skills", updated);
    }
}
