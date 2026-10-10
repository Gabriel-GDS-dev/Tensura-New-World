package com.gabriel.tensuranewworld.data;

import com.gabriel.tensuranewworld.registry.SkillAvailability;
import com.gabriel.tensuranewworld.registry.SkillCategory;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

/** Transient, server-authored view. Never saved as progression or used to authorize actions. */
public record SkillView(String id, String nameKey, String fallbackName, String descriptionKey,
        String description, String icon, SkillCategory category, double mastery, int maxMastery,
        boolean toggleable, boolean toggled, boolean canToggle, boolean temporary, int remainingTicks,
        List<Mode> modes, String unlockGuide) {
    public SkillView { modes = List.copyOf(modes); }

    public SkillView(String id, String nameKey, String fallbackName, String descriptionKey,
            String description, String icon, SkillCategory category, double mastery, int maxMastery,
            boolean toggleable, boolean toggled, boolean canToggle, boolean temporary, int remainingTicks,
            List<Mode> modes) {
        this(id, nameKey, fallbackName, descriptionKey, description, icon, category, mastery, maxMastery,
                toggleable, toggled, canToggle, temporary, remainingTicks, modes, "");
    }

    public SkillView withUnlockGuide(String guide) {
        return new SkillView(id, nameKey, fallbackName, descriptionKey, description, icon, category, mastery,
                maxMastery, toggleable, toggled, canToggle, temporary, remainingTicks, modes, guide);
    }

    public record Mode(int index, String nameKey, String name, double magiculeCost, double auraCost,
            double trainingMagiculeCost, double trainingAuraCost, int cooldownTicks, boolean slottable,
            int slot, SkillAvailability availability, String hint) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Index", index); tag.putString("NameKey", nameKey); tag.putString("Name", name);
            tag.putDouble("MP", magiculeCost); tag.putDouble("AP", auraCost);
            tag.putDouble("TrainingMP", trainingMagiculeCost); tag.putDouble("TrainingAP", trainingAuraCost);
            tag.putInt("Cooldown", cooldownTicks); tag.putBoolean("Slottable", slottable);
            tag.putInt("Slot", slot); tag.putString("State", availability.name()); tag.putString("Hint", hint);
            return tag;
        }
        static Mode load(CompoundTag tag) {
            SkillAvailability state;
            try { state = SkillAvailability.valueOf(tag.getString("State")); }
            catch (IllegalArgumentException ignored) { state = SkillAvailability.UNAVAILABLE; }
            return new Mode(tag.getInt("Index"), tag.getString("NameKey"), tag.getString("Name"),
                    tag.getDouble("MP"), tag.getDouble("AP"), tag.getDouble("TrainingMP"), tag.getDouble("TrainingAP"),
                    tag.getInt("Cooldown"), tag.getBoolean("Slottable"), tag.getInt("Slot"), state, tag.getString("Hint"));
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id); tag.putString("NameKey", nameKey); tag.putString("Name", fallbackName);
        tag.putString("DescriptionKey", descriptionKey); tag.putString("Description", description);
        tag.putString("Icon", icon); tag.putString("Category", category.name());
        tag.putString("UnlockGuide", unlockGuide);
        tag.putDouble("Mastery", mastery); tag.putInt("MaxMastery", maxMastery);
        tag.putBoolean("Toggleable", toggleable); tag.putBoolean("Toggled", toggled);
        tag.putBoolean("CanToggle", canToggle); tag.putBoolean("Temporary", temporary);
        tag.putInt("RemainingTicks", remainingTicks);
        ListTag list = new ListTag(); modes.forEach(mode -> list.add(mode.save())); tag.put("Modes", list);
        return tag;
    }

    public static SkillView load(CompoundTag tag) {
        ListTag modes = tag.getList("Modes", Tag.TAG_COMPOUND);
        List<Mode> values = new java.util.ArrayList<>();
        for (int i = 0; i < modes.size(); i++) values.add(Mode.load(modes.getCompound(i)));
        return new SkillView(tag.getString("Id"), tag.getString("NameKey"), tag.getString("Name"),
                tag.getString("DescriptionKey"), tag.getString("Description"), tag.getString("Icon"),
                SkillCategory.from(tag.getString("Category"), ""), tag.getDouble("Mastery"), tag.getInt("MaxMastery"),
                tag.getBoolean("Toggleable"), tag.getBoolean("Toggled"), tag.getBoolean("CanToggle"),
                tag.getBoolean("Temporary"), tag.getInt("RemainingTicks"), values, tag.getString("UnlockGuide"));
    }
}
