package com.gabriel.tensuranewworld.data;

import net.minecraft.nbt.CompoundTag;

public record StomachEntry(String itemId, int count) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("item_id", itemId);
        tag.putInt("count", count);
        return tag;
    }

    public static StomachEntry load(CompoundTag tag) {
        return new StomachEntry(tag.getString("item_id"), Math.max(0, tag.getInt("count")));
    }
}
