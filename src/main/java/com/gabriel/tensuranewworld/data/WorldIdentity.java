package com.gabriel.tensuranewworld.data;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent world identity; independent of player UUID, server address and world name. */
public final class WorldIdentity extends SavedData {
    private final UUID id;
    public WorldIdentity() { this(UUID.randomUUID()); setDirty(); }
    private WorldIdentity(UUID id) { this.id = id; }
    public UUID id() { return id; }
    public static WorldIdentity load(CompoundTag tag, HolderLookup.Provider registries) {
        return tag.hasUUID("Id") ? new WorldIdentity(tag.getUUID("Id")) : new WorldIdentity();
    }
    public static UUID get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WorldIdentity::new, WorldIdentity::load), "tensuranewworld_identity").id();
    }
    @Override public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putUUID("Id", id); return tag;
    }
}
