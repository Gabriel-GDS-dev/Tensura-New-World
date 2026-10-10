package com.gabriel.tensuranewworld.gameplay;

import com.gabriel.tensuranewworld.data.BodyProfile;
import com.gabriel.tensuranewworld.data.MimicForm;
import com.gabriel.tensuranewworld.data.TensuraPlayerData;
import com.gabriel.tensuranewworld.integration.MorphBridge;
import com.gabriel.tensuranewworld.registry.RaceRegistry;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.tags.EntityTypeTags;
import net.neoforged.fml.ModList;

/** Applies only the differences missing from Morph, plus body-dependent survival rules. */
public final class BodyPhysiology {
    private static final String FOOD_RESERVE = "TnwFoodReserve";
    private static final Map<ServerPlayer, RuntimeBody> STATES = new WeakHashMap<>();
    private static final class RuntimeBody {
        BodyProfile profile;
        LivingEntity fallbackEntity;
        String fallbackId = "";
        boolean ownsFlight;
        float lastMaximum;
    }
    private BodyPhysiology() { }

    public static BodyProfile profile(ServerPlayer player, TensuraPlayerData data) {
        RuntimeBody state = STATES.get(player);
        return state != null && state.profile != null ? state.profile : original(data);
    }

    private static BodyProfile original(TensuraPlayerData data) {
        String name = RaceRegistry.get(data.getRaceId()).map(race -> race.displayName()).orElse(data.getRaceId());
        return new BodyProfile("", name, BodyRules.raceNeedsFood(data.getRaceId()), false, false, false, false);
    }

    public static void tick(ServerPlayer player, TensuraPlayerData data) {
        if (!player.isAlive()) return;
        RuntimeBody runtime = STATES.computeIfAbsent(player, ignored -> new RuntimeBody());
        MorphBridge.State morph = MorphBridge.current(player);
        // Morph owns selection and rendering whenever it is installed. Ignore stale NW selection.
        MimicForm form = ModList.get().isLoaded("morph") ? null : data.getSelectedMimicForm();
        LivingEntity entity = morph == null ? fallbackEntity(player, runtime, form) : morph.entity();
        boolean transition = morph != null && morph.transitioning();
        BodyProfile body = original(data);
        if (entity != null) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            boolean flight = entity instanceof FlyingAnimal || entity instanceof FlyingMob || entity instanceof Bat
                    || morph != null && morph.nativeFlight();
            boolean food = !(entity instanceof Slime) && !(entity instanceof net.minecraft.world.entity.animal.AbstractGolem)
                    && !entity.getType().is(EntityTypeTags.UNDEAD)
                    && bodyNeedsFood(id);
            body = new BodyProfile(id, entity.getType().getDescription().getString(), food, flight,
                    entity.canBreatheUnderwater(), entity.fireImmune(), false);
        } else if (transition && runtime.profile != null) {
            BodyProfile previous = runtime.profile;
            body = new BodyProfile(previous.entityId(), previous.name(), previous.needsFood(), previous.canFly(),
                    previous.breathesWater(), previous.fireImmune(), true);
        }

        float oldMaximum = player.getMaxHealth();
        float oldHealth = player.getHealth();
        // Morph removes the old native health modifier on demorph without rescaling
        // health. Use the last completed tick's maximum for this specific handoff.
        float ratioMaximum = morph == null && ModList.get().isLoaded("morph")
                && runtime.profile != null && !runtime.profile.entityId().isBlank()
                && runtime.lastMaximum > 0 ? runtime.lastMaximum : oldMaximum;
        CompoundTag nativeAttributes = morph == null ? new CompoundTag() : morph.attributes();
        double capturedSpeed = entity == null ? 0 : data.getMimicForms().stream()
                .filter(known -> known.entityId().equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()))
                .mapToDouble(MimicForm::movementSpeed).findFirst().orElse(0);
        updateAttribute(player, entity, form, nativeAttributes, Attributes.MAX_HEALTH, "mimic_health", 20, capturedSpeed);
        updateAttribute(player, entity, form, nativeAttributes, Attributes.ATTACK_DAMAGE, "mimic_attack", 1, capturedSpeed);
        updateAttribute(player, entity, form, nativeAttributes, Attributes.MOVEMENT_SPEED, "mimic_speed", .1, capturedSpeed);
        updateAttribute(player, entity, form, nativeAttributes, Attributes.ARMOR, "mimic_armor", 0, capturedSpeed);
        updateAttribute(player, entity, null, nativeAttributes, Attributes.ARMOR_TOUGHNESS, "mimic_toughness", 0, capturedSpeed);
        updateAttribute(player, entity, null, nativeAttributes, Attributes.KNOCKBACK_RESISTANCE, "mimic_knockback", 0, capturedSpeed);
        updateAttribute(player, entity, null, nativeAttributes, Attributes.ATTACK_KNOCKBACK, "mimic_attack_knockback", 0, capturedSpeed);
        updateAttribute(player, entity, null, nativeAttributes, Attributes.JUMP_STRENGTH, "mimic_jump", .42, capturedSpeed);
        if (player.getMaxHealth() != ratioMaximum) {
            player.setHealth(BodyRules.rescaledHealth(oldHealth, ratioMaximum, player.getMaxHealth()));
        }
        // Native Morph traits handle their own flight. Own only flight that we introduce.
        boolean wantsFlight = body.canFly();
        if (wantsFlight && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            runtime.ownsFlight = true;
            player.onUpdateAbilities();
        } else if (!wantsFlight && runtime.ownsFlight) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
            runtime.ownsFlight = false;
        }
        if (wantsFlight) player.fallDistance = 0;
        if (entity != null) {
            if (body.breathesWater()) player.setAirSupply(player.getMaxAirSupply());
            // Chickens glide; they are not treated as fully flying birds.
            if (entity instanceof net.minecraft.world.entity.animal.Chicken && player.getDeltaMovement().y < 0) {
                player.fallDistance = 0;
                var motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, Math.max(motion.y, -.15), motion.z);
            }
        }
        runtime.profile = body;
        runtime.lastMaximum = player.getMaxHealth();
        updateFood(player, body.needsFood());
    }

    private static boolean bodyNeedsFood(String id) {
        // Modded spiritual and inorganic bodies use the same lineage rules as their race.
        return BodyRules.raceNeedsFood(id);
    }

    private static LivingEntity fallbackEntity(ServerPlayer player, RuntimeBody runtime, MimicForm form) {
        if (form == null) { runtime.fallbackEntity = null; runtime.fallbackId = ""; return null; }
        if (!runtime.fallbackId.equals(form.entityId()) || runtime.fallbackEntity == null
                || runtime.fallbackEntity.level() != player.level()) {
            runtime.fallbackId = form.entityId();
            ResourceLocation id = ResourceLocation.tryParse(form.entityId());
            var type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
            var entity = type == null ? null : type.create(player.level());
            runtime.fallbackEntity = entity instanceof LivingEntity living ? living : null;
        }
        return runtime.fallbackEntity;
    }

    private static void updateAttribute(ServerPlayer player, LivingEntity entity, MimicForm form,
            CompoundTag nativeAttributes, Holder<Attribute> attribute, String path, double fallback, double capturedSpeed) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        ResourceLocation ownId = ResourceLocation.fromNamespaceAndPath("tensuranewworld", path);
        double amount = 0;
        if (entity != null) {
            String key = BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()).toString();
            double target = nativeAttributes.contains("attr_" + key)
                    ? nativeAttributes.getDouble("attr_" + key) : value(entity, attribute, fallback);
            if (form != null) {
                if (attribute == Attributes.MAX_HEALTH) target = form.maxHealth();
                else if (attribute == Attributes.ATTACK_DAMAGE) target = form.attackDamage();
                else if (attribute == Attributes.MOVEMENT_SPEED) target = form.movementSpeed();
                else if (attribute == Attributes.ARMOR) target = form.armor();
            } else if (attribute == Attributes.MOVEMENT_SPEED) {
                // Morph's stock 0.1 cap must not make horses as slow as a player.
                target = capturedSpeed > 0 ? capturedSpeed : value(entity, attribute, target);
            }
            var nativeId = ResourceLocation.fromNamespaceAndPath("morph", "attr_" + ResourceLocation.parse(key).getPath());
            var nativeModifier = instance.getModifier(nativeId);
            amount = BodyRules.correction(target, instance.getBaseValue(), nativeModifier == null ? 0 : nativeModifier.amount());
        }
        var current = instance.getModifier(ownId);
        if (current != null && Math.abs(current.amount() - amount) < 1E-9 || current == null && Math.abs(amount) < 1E-9) return;
        instance.removeModifier(ownId);
        if (Math.abs(amount) >= 1E-9) instance.addTransientModifier(new AttributeModifier(ownId, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static double value(LivingEntity entity, Holder<Attribute> attribute, double fallback) {
        var instance = entity.getAttribute(attribute);
        return instance == null ? fallback : instance.getBaseValue();
    }

    private static void updateFood(ServerPlayer player, boolean needsFood) {
        var persistent = player.getPersistentData();
        var food = player.getFoodData();
        if (!needsFood) {
            if (!persistent.contains(FOOD_RESERVE)) {
                var reserve = new CompoundTag();
                food.addAdditionalSaveData(reserve);
                persistent.put(FOOD_RESERVE, reserve);
            }
            food.setFoodLevel(20);
            food.setSaturation(0);
            food.setExhaustion(0);
        } else if (persistent.contains(FOOD_RESERVE)) {
            food.readAdditionalSaveData(persistent.getCompound(FOOD_RESERVE));
            persistent.remove(FOOD_RESERVE);
        }
    }

    public static void beforeTick(ServerPlayer player, TensuraPlayerData data) {
        // Protect foodless bodies before vanilla processes starvation and exhaustion.
        updateFood(player, profile(player, data).needsFood());
    }
}
