package com.gabriel.tensuranewworld.data;

import net.minecraft.nbt.CompoundTag;

/** Transient, server-authoritative information about the current body. */
public record BodyProfile(String entityId, String name, boolean needsFood, boolean canFly,
        boolean breathesWater, boolean fireImmune, boolean transitioning) {
    public static final String SNAPSHOT_KEY = "CurrentBody";

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", entityId);
        tag.putString("name", name);
        tag.putBoolean("food", needsFood);
        tag.putBoolean("flight", canFly);
        tag.putBoolean("water", breathesWater);
        tag.putBoolean("fire", fireImmune);
        tag.putBoolean("transition", transitioning);
        return tag;
    }

    public static BodyProfile load(CompoundTag tag) {
        return new BodyProfile(tag.getString("id"), tag.getString("name"), tag.getBoolean("food"),
                tag.getBoolean("flight"), tag.getBoolean("water"), tag.getBoolean("fire"),
                tag.getBoolean("transition"));
    }
}
