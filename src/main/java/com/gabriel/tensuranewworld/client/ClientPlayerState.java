package com.gabriel.tensuranewworld.client;

import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public final class ClientPlayerState {
    private static TensuraPlayerData snapshot = new TensuraPlayerData();
    private static boolean morphAllowed;
    private static com.gabriel.tensuranewworld.network.TargetVitalsPayload target;
    private static long targetReceived;
    public static boolean morphAllowed() { return morphAllowed; }
    public static void acceptTarget(com.gabriel.tensuranewworld.network.TargetVitalsPayload value) { target = value; targetReceived = System.nanoTime(); }
    public static com.gabriel.tensuranewworld.network.TargetVitalsPayload target() { return System.nanoTime() - targetReceived < 750_000_000L ? target : null; }
    private static com.gabriel.tensuranewworld.data.BodyProfile body;
    private static java.util.List<com.gabriel.tensuranewworld.data.SkillView> skills = java.util.List.of();
    private static final Map<UUID, String> mimicForms = new HashMap<>();

    private ClientPlayerState() {
    }

    public static void accept(CompoundTag tag) {
        snapshot = TensuraPlayerData.load(tag.copy());
        morphAllowed = tag.getBoolean("MorphAllowed");
        body = tag.contains(com.gabriel.tensuranewworld.data.BodyProfile.SNAPSHOT_KEY)
                ? com.gabriel.tensuranewworld.data.BodyProfile.load(tag.getCompound(
                        com.gabriel.tensuranewworld.data.BodyProfile.SNAPSHOT_KEY)) : null;
        var list = tag.getList(com.gabriel.tensuranewworld.integration.ReincarnatedSkills.SNAPSHOT_KEY,
                net.minecraft.nbt.Tag.TAG_COMPOUND);
        var updated = new java.util.ArrayList<com.gabriel.tensuranewworld.data.SkillView>();
        for (int i = 0; i < list.size(); i++) updated.add(com.gabriel.tensuranewworld.data.SkillView.load(list.getCompound(i)));
        skills = java.util.List.copyOf(updated);
    }

    public static TensuraPlayerData get() {
        return snapshot;
    }

    public static void reset() {
        snapshot = new TensuraPlayerData();
        morphAllowed = false;
        target = null;
        skills = java.util.List.of();
        body = null;
        mimicForms.clear();
    }

    public static com.gabriel.tensuranewworld.data.BodyProfile body() {
        return body != null ? body : new com.gabriel.tensuranewworld.data.BodyProfile("", "",
                com.gabriel.tensuranewworld.gameplay.BodyRules.raceNeedsFood(snapshot.getRaceId()),
                false, false, false, false);
    }

    public static java.util.List<com.gabriel.tensuranewworld.data.SkillView> skills() {
        return skills;
    }

    public static void acceptMimicForm(UUID playerId, String entityId) {
        if (entityId.isBlank()) {
            mimicForms.remove(playerId);
        } else {
            mimicForms.put(playerId, entityId);
        }
    }

    public static String getMimicForm(UUID playerId) {
        return mimicForms.getOrDefault(playerId, "");
    }
}
