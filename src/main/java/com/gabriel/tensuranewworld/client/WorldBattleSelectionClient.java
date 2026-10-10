package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.integration.ReincarnatedSkills;
import com.gabriel.tensuranewworld.network.BattleSelectionPayload;
import com.gabriel.tensuranewworld.network.WorldSessionPayload;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public final class WorldBattleSelectionClient {
    private static UUID world;
    private static final LinkedHashSet<String> selected = new LinkedHashSet<>();
    private WorldBattleSelectionClient() { }
    public static void accept(WorldSessionPayload payload) {
        world = payload.world(); selected.clear(); selected.addAll(payload.skills());
    }
    public static void reset() { world = null; selected.clear(); }
    public static UUID world() { return world; }
    public static List<String> all() {
        var player = Minecraft.getInstance().player;
        if (world == null || player == null) return List.of();
        Object storage = ReincarnatedSkills.callStatic(ReincarnatedSkills.API, "getSkillsFrom", player);
        Object learned = ReincarnatedSkills.call(storage, "getLearnedSkills");
        var valid = new LinkedHashSet<String>();
        if (learned instanceof Collection<?> instances) for (var instance : instances) {
            Object skill = ReincarnatedSkills.call(instance, "getSkill");
            Object mastery = ReincarnatedSkills.call(instance, "getMastery");
            String id = String.valueOf(ReincarnatedSkills.call(instance, "getSkillId"));
            if (mastery instanceof Number points && points.doubleValue() >= 0 && !id.equals("greatsage:raphael")
                    && !"RESISTANCE".equalsIgnoreCase(String.valueOf(ReincarnatedSkills.call(skill, "getType"))) && selected.contains(id)) valid.add(id);
        }
        return List.copyOf(valid);
    }
    public static void toggle(ResourceLocation id) {
        if (world == null || id == null) return;
        String key = id.toString();
        boolean on = !selected.remove(key);
        if (on) selected.add(key);
        PacketDistributor.sendToServer(new BattleSelectionPayload(world, key, on));
    }
    public static void clear() {
        if (world == null) return;
        for (var id : List.copyOf(selected)) PacketDistributor.sendToServer(new BattleSelectionPayload(world, id, false));
        selected.clear();
    }
}
