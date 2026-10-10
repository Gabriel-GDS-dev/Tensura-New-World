package com.gabriel.tensuranewworld.data;

import net.minecraft.nbt.CompoundTag;

public record MimicForm(
        String entityId,
        String displayName,
        int defeatedCount,
        float maxHealth,
        float attackDamage,
        float movementSpeed,
        float armor,
        boolean fireImmune,
        boolean waterBreathing) {

    public MimicForm {
        defeatedCount = Math.max(1, defeatedCount);
        maxHealth = finiteAtLeast(maxHealth, 1.0F);
        attackDamage = finiteAtLeast(attackDamage, 1.0F);
        movementSpeed = finiteAtLeast(movementSpeed, 0.01F);
        armor = finiteAtLeast(armor, 0.0F);
    }

    public MimicForm addDefeat(float health, float damage, float speed, float armorValue,
            boolean isFireImmune, boolean canBreatheUnderwater) {
        return new MimicForm(entityId, displayName, defeatedCount + 1,
                Math.max(maxHealth, health), Math.max(attackDamage, damage),
                Math.max(movementSpeed, speed), Math.max(armor, armorValue),
                fireImmune || isFireImmune, waterBreathing || canBreatheUnderwater);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("entity_id", entityId);
        tag.putString("display_name", displayName);
        tag.putInt("defeated_count", defeatedCount);
        tag.putFloat("max_health", maxHealth);
        tag.putFloat("attack_damage", attackDamage);
        tag.putFloat("movement_speed", movementSpeed);
        tag.putFloat("armor", armor);
        tag.putBoolean("fire_immune", fireImmune);
        tag.putBoolean("water_breathing", waterBreathing);
        return tag;
    }

    public static MimicForm load(CompoundTag tag) {
        return new MimicForm(
                tag.getString("entity_id"),
                tag.getString("display_name"),
                tag.getInt("defeated_count"),
                tag.getFloat("max_health"),
                tag.getFloat("attack_damage"),
                tag.getFloat("movement_speed"),
                tag.getFloat("armor"),
                tag.getBoolean("fire_immune"),
                tag.getBoolean("water_breathing"));
    }

    private static float finiteAtLeast(float value, float minimum) {
        return Float.isFinite(value) ? Math.max(minimum, value) : minimum;
    }
}
