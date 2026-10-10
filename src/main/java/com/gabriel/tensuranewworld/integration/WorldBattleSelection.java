package com.gabriel.tensuranewworld.integration;

import com.gabriel.tensuranewworld.data.WorldIdentity;
import io.github.manasmods.manascore.skill.api.SkillAPI;
import io.github.manasmods.tensura.ability.skill.Skill;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class WorldBattleSelection {
    private static final String KEY = "tnw_battle_selection";
    private WorldBattleSelection() { }
    public static boolean eligible(ServerPlayer player, String id) {
        var parsed = ResourceLocation.tryParse(id);
        if (parsed == null || id.equals("greatsage:raphael")) return false;
        return SkillAPI.getSkillsFrom(player).getSkill(parsed).filter(instance -> instance.getMastery() >= 0
                && instance.getSkill() instanceof Skill skill && skill.getType() != Skill.SkillType.RESISTANCE).isPresent();
    }
    public static List<String> get(ServerPlayer player) {
        var root = player.getPersistentData().getCompound(KEY);
        var world = WorldIdentity.get(player.getServer());
        if (!root.hasUUID("World") || !world.equals(root.getUUID("World"))) return List.of();
        var result = new LinkedHashSet<String>();
        var ids = root.getList("Skills", 8);
        for (int index = 0; index < ids.size(); index++) if (eligible(player, ids.getString(index))) result.add(ids.getString(index));
        return List.copyOf(result);
    }
    public static boolean update(ServerPlayer player, UUID world, String id, boolean selected) {
        if (!world.equals(WorldIdentity.get(player.getServer())) || (selected && !eligible(player, id))) return false;
        var ids = new ArrayList<>(get(player));
        ids.remove(id);
        if (selected) ids.add(id);
        var root = new CompoundTag();
        root.putUUID("World", world);
        var list = new ListTag();
        ids.forEach(value -> list.add(StringTag.valueOf(value)));
        root.put("Skills", list);
        player.getPersistentData().put(KEY, root);
        return true;
    }
    public static void clone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        if (oldPlayer.getPersistentData().contains(KEY)) newPlayer.getPersistentData().put(KEY, oldPlayer.getPersistentData().getCompound(KEY).copy());
    }
}
