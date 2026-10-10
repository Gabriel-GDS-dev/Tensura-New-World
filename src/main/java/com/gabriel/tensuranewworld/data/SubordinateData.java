package com.gabriel.tensuranewworld.data;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public record SubordinateData(UUID entityId, String name, String raceId, long existencePoints, boolean giftGranted) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("entity_id", entityId);
        tag.putString("name", name);
        tag.putString("race_id", raceId);
        tag.putLong("ep", existencePoints);
        tag.putBoolean("gift_granted", giftGranted);
        return tag;
    }

    public static SubordinateData load(CompoundTag tag) {
        UUID id = tag.hasUUID("entity_id") ? tag.getUUID("entity_id") : UUID.randomUUID();
        return new SubordinateData(
                id,
                tag.getString("name"),
                tag.getString("race_id"),
                tag.getLong("ep"),
                tag.getBoolean("gift_granted"));
    }

    public SubordinateData withGift(long newEp) {
        return new SubordinateData(entityId, name, raceId, newEp, true);
    }
}
